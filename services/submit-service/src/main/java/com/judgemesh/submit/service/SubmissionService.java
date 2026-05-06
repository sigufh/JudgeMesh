package com.judgemesh.submit.service;

import com.judgemesh.api.client.ProblemClient;
import com.judgemesh.api.dto.ContestRankDTO;
import com.judgemesh.api.dto.SubmissionDTO;
import com.judgemesh.api.dto.SubmitCreateRequest;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.enumx.LanguageType;
import com.judgemesh.api.enumx.SubmitStatus;
import com.judgemesh.api.error.ErrorCode;
import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.submit.config.SubmitProperties;
import com.judgemesh.submit.error.DomainException;
import com.judgemesh.submit.model.ContestRecord;
import com.judgemesh.submit.model.SubmissionRecord;
import com.judgemesh.submit.repository.SubmitStateRepository;
import com.judgemesh.submit.websocket.ContestRankSocketHub;
import com.judgemesh.submit.websocket.SubmissionSocketHub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmitStateRepository repository;
    private final ContestService contestService;
    private final SubmitDedupService dedupService;
    private final JudgeTaskPublisher taskPublisher;
    private final LeaderboardService leaderboardService;
    private final SubmitProperties properties;
    private final ObjectProvider<ProblemClient> problemClientProvider;
    private final SubmissionSocketHub submissionSocketHub;
    private final ContestRankSocketHub contestRankSocketHub;

    public SubmissionDTO submit(long userId, SubmitCreateRequest request) {
        validateCodeLength(request.getCode());
        if (!dedupService.tryAcquire(userId, request.getProblemId())) {
            throw new DomainException(ErrorCode.SUBMIT_RATE_LIMITED);
        }

        ContestRecord contest = null;
        if (request.getContestId() != null) {
            contest = contestService.getContestRecord(request.getContestId());
            contestService.assertSubmissionAllowed(request.getContestId(), userId, request.getProblemId());
        }

        ProblemDTO problem = resolveProblem(request.getProblemId());
        SubmissionRecord record = repository.saveSubmission(SubmissionRecord.builder()
                .userId(userId)
                .problemId(request.getProblemId())
                .contestId(request.getContestId())
                .language(request.getLanguage())
                .code(request.getCode())
                .status(SubmitStatus.PENDING)
                .submittedAt(Instant.now())
                .build());

        JudgeTask task = buildTask(record, request, problem);
        taskPublisher.publish(task);
        submissionSocketHub.broadcast(record.getId(), toDto(record));
        if (contest != null && contestService.isContestFrozen(contest)) {
            contestRankSocketHub.broadcast(contest.getId(), contestService.contestRank(contest.getId()));
        }
        return toDto(record);
    }

    public SubmissionDTO getSubmission(long id) {
        return toDto(getSubmissionRecord(id));
    }

    public List<SubmissionDTO> listMine(long userId) {
        return repository.findSubmissionsByUser(userId).stream().map(this::toDto).toList();
    }

    public SubmissionDTO applyJudgeResult(JudgeResult result) {
        if (result == null || result.getSubmitId() == null) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "judge result must contain submit_id");
        }
        SubmissionRecord record = getSubmissionRecord(result.getSubmitId());
        SubmitStatus status = SubmitStatus.fromValue(result.getStatus());
        record.setStatus(status);
        record.setJudgeMessage(result.getMessage());
        record.setTimeUsedMs(result.getTimeUsedMs());
        record.setMemoryUsedKb(result.getMemoryUsedKb());
        record.setJudgedByWorker(result.getWorkerId());
        record.setJudgedAt(Instant.now());
        record.setScore(scoreFor(status, result));
        repository.saveSubmission(record);

        ContestRecord contest = record.getContestId() == null ? null : contestService.getContestRecord(record.getContestId());
        leaderboardService.recordResult(record, contest, status, record.getJudgedAt());
        submissionSocketHub.broadcast(record.getId(), toDto(record));
        if (contest != null && !contestService.isContestFrozen(contest)) {
            ContestRankDTO rankDTO = contestService.contestRank(contest.getId());
            contestRankSocketHub.broadcast(contest.getId(), rankDTO);
        }
        return toDto(record);
    }

    private SubmissionRecord getSubmissionRecord(long id) {
        return repository.findSubmission(id)
                .orElseThrow(() -> new DomainException(ErrorCode.SUBMIT_NOT_FOUND, "submitId=" + id));
    }

    private void validateCodeLength(String code) {
        if (code == null) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "code is required");
        }
        if (code.length() > properties.getSubmission().getCodeLengthLimit()) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "code length exceeds limit");
        }
    }

    private ProblemDTO resolveProblem(long problemId) {
        ProblemClient problemClient = problemClientProvider.getIfAvailable();
        if (problemClient != null) {
            try {
                ProblemDTO dto = problemClient.getById(problemId);
                if (dto != null) {
                    return dto;
                }
            } catch (Exception ex) {
                log.debug("problem-service unavailable for problem {}", problemId, ex);
            }
        }
        return ProblemDTO.builder()
                .id(problemId)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .testcaseManifestUrl(defaultTestcaseManifestUrl(problemId))
                .build();
    }

    private JudgeTask buildTask(SubmissionRecord record, SubmitCreateRequest request, ProblemDTO problem) {
        Integer timeLimitMs = Optional.ofNullable(request.getTimeLimitMs())
                .orElse(problem.getTimeLimitMs() != null ? problem.getTimeLimitMs() : 1000);
        Integer memoryLimitMb = Optional.ofNullable(request.getMemoryLimitMb())
                .orElse(problem.getMemoryLimitMb() != null ? problem.getMemoryLimitMb() : 256);
        String manifestUrl = Optional.ofNullable(request.getTestcaseManifestUrl())
                .filter(value -> !value.isBlank())
                .orElseGet(() -> Optional.ofNullable(problem.getTestcaseManifestUrl())
                        .filter(value -> !value.isBlank())
                        .orElse(defaultTestcaseManifestUrl(record.getProblemId())));

        return JudgeTask.builder()
                .submitId(record.getId())
                .problemId(record.getProblemId())
                .source(record.getCode())
                .language(record.getLanguage().toWireValue())
                .timeLimitMs(timeLimitMs)
                .memoryLimitMb(memoryLimitMb)
                .testcaseManifestUrl(manifestUrl)
                .testcases(new ArrayList<>())
                .callbackUrl(properties.getSubmission().getCallbackUrl())
                .retryCount(0)
                .build();
    }

    private String defaultTestcaseManifestUrl(long problemId) {
        return "http://problem-service/api/problems/%s/testcase/manifest".formatted(problemId);
    }

    private int scoreFor(SubmitStatus status, JudgeResult result) {
        if (status != SubmitStatus.AC) {
            return 0;
        }
        return Optional.ofNullable(result.getTimeUsedMs()).orElse(0);
    }

    private SubmissionDTO toDto(SubmissionRecord record) {
        return SubmissionDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .problemId(record.getProblemId())
                .contestId(record.getContestId())
                .language(record.getLanguage())
                .code(record.getCode())
                .status(record.getStatus())
                .score(record.getScore())
                .timeUsedMs(record.getTimeUsedMs())
                .memoryUsedKb(record.getMemoryUsedKb())
                .judgeMessage(record.getJudgeMessage())
                .judgedByWorker(record.getJudgedByWorker())
                .submittedAt(record.getSubmittedAt())
                .judgedAt(record.getJudgedAt())
                .build();
    }
}
