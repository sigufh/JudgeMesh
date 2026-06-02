package com.judgemesh.submit.service;

import com.judgemesh.api.client.ProblemClient;
import com.judgemesh.api.client.UserClient;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.dto.SubmissionDTO;
import com.judgemesh.api.dto.SubmitCreateRequest;
import com.judgemesh.api.dto.SubmitDTO;
import com.judgemesh.api.enumx.LanguageType;
import com.judgemesh.api.enumx.SubmitStatus;
import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.submit.domain.Contest;
import com.judgemesh.submit.domain.Submission;
import com.judgemesh.submit.domain.SubmissionStatus;
import com.judgemesh.submit.repository.SubmissionStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class SubmissionService {
    private final SubmissionStore store;
    private final ContestService contestService;
    private final RankingService rankingService;
    private final SubmitDedupService dedupService;
    private final ObjectProvider<ProblemClient> problemClient;
    private final ObjectProvider<UserClient> userClient;
    private final ObjectProvider<RabbitTemplate> rabbitTemplate;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplate;
    private final String exchange;
    private final String submitRoutingKey;
    private final String callbackUrl;
    private final String problemBaseUrl;
    private final boolean deductScoreEnabled;
    private final int submitCost;
    private final int codeLengthLimit;
    private final RestTemplate directProblemClient = new RestTemplate();

    public SubmissionService(
            SubmissionStore store,
            ContestService contestService,
            RankingService rankingService,
            SubmitDedupService dedupService,
            ObjectProvider<ProblemClient> problemClient,
            ObjectProvider<UserClient> userClient,
            ObjectProvider<RabbitTemplate> rabbitTemplate,
            ObjectProvider<SimpMessagingTemplate> messagingTemplate,
            @Value("${judgemesh.mq.submit-exchange:judgemesh.exchange}") String exchange,
            @Value("${judgemesh.mq.submit-routing-key:judge.task}") String submitRoutingKey,
            @Value("${judgemesh.submit.callback-url:http://submit-service:8083/api/submit/internal/result}") String callbackUrl,
            @Value("${judgemesh.problem.base-url:http://127.0.0.1:8082}") String problemBaseUrl,
            @Value("${judgemesh.submit.deduct-score-enabled:false}") boolean deductScoreEnabled,
            @Value("${judgemesh.submit.submit-cost:1}") int submitCost,
            @Value("${judgemesh.submit.code-length-limit:65536}") int codeLengthLimit) {
        this.store = store;
        this.contestService = contestService;
        this.rankingService = rankingService;
        this.dedupService = dedupService;
        this.problemClient = problemClient;
        this.userClient = userClient;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
        this.exchange = exchange;
        this.submitRoutingKey = submitRoutingKey;
        this.callbackUrl = callbackUrl;
        this.problemBaseUrl = problemBaseUrl;
        this.deductScoreEnabled = deductScoreEnabled;
        this.submitCost = submitCost;
        this.codeLengthLimit = codeLengthLimit;
    }

    @GlobalTransactional(name = "submit-create-api", rollbackFor = Exception.class)
    public SubmissionDTO submit(Long userId, SubmitCreateRequest request) {
        SubmitCommand command = new SubmitCommand(
                userId,
                request.getProblemId(),
                request.getContestId(),
                request.getLanguage() == null ? null : request.getLanguage().toWireValue(),
                request.getCode(),
                request.getTimeLimitMs(),
                request.getMemoryLimitMb(),
                request.getTestcaseManifestUrl());
        return toSubmissionDto(submitInternal(command));
    }

    @GlobalTransactional(name = "submit-create", rollbackFor = Exception.class)
    public SubmitDTO submit(SubmitCommand command) {
        return toDto(submitInternal(command));
    }

    private Submission submitInternal(SubmitCommand command) {
        validateCode(command.code());
        Long userId = command.userId() == null ? 1002L : command.userId();
        if (!dedupService.tryAcquire(userId, command.problemId())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "submit too frequently");
        }
        deductSubmitCostIfEnabled(userId);

        Contest contest = null;
        if (command.contestId() != null) {
            contest = contestService.get(command.contestId());
            contestService.assertSubmissionAllowed(command.contestId(), userId, command.problemId());
        }

        String language = normalizeLanguage(command.language());
        Submission submission = Submission.builder()
                .userId(userId)
                .problemId(command.problemId())
                .contestId(command.contestId())
                .language(language)
                .code(command.code())
                .codeLength(command.code().length())
                .status(SubmissionStatus.PENDING)
                .score(0)
                .submittedAt(Instant.now())
                .build();
        store.save(submission);
        publishTask(submission, command);
        notifySubmission(submission);
        if (contest != null && contestService.isContestFrozen(contest)) {
            notifyContestRank(contest.getId());
        }
        return submission;
    }

    public SubmissionDTO getSubmission(Long id) {
        return toSubmissionDto(find(id));
    }

    public SubmitDTO get(Long id) {
        return toDto(find(id));
    }

    public List<SubmissionDTO> listMine(Long userId) {
        return store.findByUser(userId).stream().map(this::toSubmissionDto).toList();
    }

    public List<SubmitDTO> mine(Long userId) {
        return store.findByUser(userId).stream().map(this::toDto).toList();
    }

    public SubmissionDTO applyJudgeResult(JudgeResult result) {
        return toSubmissionDto(applyResultInternal(result));
    }

    public SubmitDTO applyResult(JudgeResult result) {
        return toDto(applyResultInternal(result));
    }

    public SubmitDTO markJudging(Long id, String workerId) {
        Submission submission = find(id);
        submission.setStatus(SubmissionStatus.JUDGING);
        submission.setJudgedByWorker(workerId);
        store.save(submission);
        notifySubmission(submission);
        return toDto(submission);
    }

    public SubmitDTO toDto(Submission submission) {
        return SubmitDTO.builder()
                .id(submission.getId())
                .userId(submission.getUserId())
                .problemId(submission.getProblemId())
                .contestId(submission.getContestId())
                .language(submission.getLanguage())
                .status(submission.getStatus().name())
                .score(submission.getScore())
                .timeUsedMs(submission.getTimeUsedMs())
                .memoryUsedKb(submission.getMemoryUsedKb())
                .judgeMessage(submission.getJudgeMessage())
                .judgedByWorker(submission.getJudgedByWorker())
                .submittedAt(submission.getSubmittedAt())
                .judgedAt(submission.getJudgedAt())
                .createdAt(submission.getSubmittedAt())
                .build();
    }

    private Submission applyResultInternal(JudgeResult result) {
        if (result == null || result.getSubmitId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "judge result must contain submit_id");
        }
        Submission submission = find(result.getSubmitId());
        SubmissionStatus status = SubmissionStatus.valueOf(result.getStatus().toUpperCase(Locale.ROOT));
        submission.setStatus(status);
        submission.setScore(status == SubmissionStatus.AC ? 100 : scoreFromCases(result.getCases()));
        submission.setTimeUsedMs(result.getTimeUsedMs());
        submission.setMemoryUsedKb(result.getMemoryUsedKb());
        submission.setJudgeMessage(result.getMessage());
        submission.setJudgedByWorker(result.getWorkerId());
        submission.setJudgedAt(Instant.now());
        store.save(submission);

        if (status == SubmissionStatus.AC) {
            Instant contestStart = null;
            if (submission.getContestId() != null) {
                contestStart = contestService.get(submission.getContestId()).getStartTime();
            }
            rankingService.accepted(submission, contestStart);
            notifyGlobalRank();
        }
        notifySubmission(submission);
        if (submission.getContestId() != null) {
            notifyContestRank(submission.getContestId());
        }
        return submission;
    }

    private Submission find(Long id) {
        return store.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "submission not found"));
    }

    private void publishTask(Submission submission, SubmitCommand command) {
        ProblemDTO problem = loadProblem(submission.getProblemId());
        List<JudgeTask.TestCaseRef> testcases = loadManifest(submission.getProblemId(), command.testcaseManifestUrl());
        JudgeTask task = JudgeTask.builder()
                .submitId(submission.getId())
                .problemId(submission.getProblemId())
                .source(submission.getCode())
                .language(submission.getLanguage())
                .timeLimitMs(resolveInt(command.timeLimitMs(), problem.getTimeLimitMs(), 1000))
                .memoryLimitMb(resolveInt(command.memoryLimitMb(), problem.getMemoryLimitMb(), 256))
                .testcaseManifestUrl(resolveManifestUrl(command.testcaseManifestUrl(), problem.getTestcaseManifestUrl(), submission.getProblemId()))
                .testcases(testcases)
                .callbackUrl(callbackUrl)
                .retryCount(0)
                .build();
        try {
            RabbitTemplate template = rabbitTemplate.getIfAvailable();
            if (template != null) {
                template.convertAndSend(exchange, submitRoutingKey, task);
            } else {
                submission.setJudgeMessage("RabbitTemplate unavailable; task stored as PENDING");
            }
        } catch (AmqpException ex) {
            submission.setJudgeMessage("RabbitMQ unavailable; task stored as PENDING: " + ex.getMessage());
        }
    }

    private void deductBalanceIfPossible(Long userId) {
        UserClient client = userClient.getIfAvailable();
        if (client != null) {
            client.deductBalance(userId, 1);
        }
    }

    private ProblemDTO loadProblem(Long problemId) {
        try {
            ProblemClient client = problemClient.getIfAvailable();
            if (client != null) {
                return client.getById(problemId);
            }
        } catch (RuntimeException ex) {
            log.debug("problem-service unavailable for problem {}", problemId, ex);
        }
        return ProblemDTO.builder()
                .id(problemId)
                .title("problem-" + problemId)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .testcaseManifestUrl(defaultTestcaseManifestUrl(problemId))
                .build();
    }

    private List<JudgeTask.TestCaseRef> loadManifest(Long problemId, String explicitManifestUrl) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                ProblemClient client = problemClient.getIfAvailable();
                if (client != null) {
                    List<JudgeTask.TestCaseRef> refs = client.getManifest(problemId);
                    if (refs != null && !refs.isEmpty()) {
                        return refs;
                    }
                }
                List<JudgeTask.TestCaseRef> refs = loadManifestDirect(problemId);
                if (!refs.isEmpty()) {
                    return refs;
                }
            } catch (RuntimeException ex) {
                lastError = ex;
            }
        }
        if (explicitManifestUrl != null && !explicitManifestUrl.isBlank()) {
            return List.of();
        }
        String message = lastError == null ? "empty testcase manifest" : lastError.getMessage();
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "testcase manifest unavailable for problem " + problemId + ": " + message);
    }

    private List<JudgeTask.TestCaseRef> loadManifestDirect(Long problemId) {
        ResponseEntity<List<JudgeTask.TestCaseRef>> response = directProblemClient.exchange(
                problemBaseUrl + "/api/problem/internal/{id}/testcase/manifest",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                problemId);
        List<JudgeTask.TestCaseRef> refs = response.getBody();
        return refs == null ? List.of() : refs;
    }

    private void deductSubmitCostIfEnabled(Long userId) {
        if (!deductScoreEnabled) {
            return;
        }
        UserClient client = userClient.getIfAvailable();
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "user-service client is unavailable");
        }
        client.deductBalance(userId, submitCost);
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code is required");
        }
        if (code.length() > codeLengthLimit) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code length exceeds limit");
        }
    }

    private static String normalizeLanguage(String language) {
        LanguageType type = LanguageType.fromValue(language);
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language is required");
        }
        return type.toWireValue();
    }

    private static int resolveInt(Integer preferred, Integer fallback, int defaultValue) {
        if (preferred != null) {
            return preferred;
        }
        return fallback == null ? defaultValue : fallback;
    }

    private static String resolveManifestUrl(String preferred, String fallback, Long problemId) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return defaultTestcaseManifestUrl(problemId);
    }

    private static String defaultTestcaseManifestUrl(Long problemId) {
        return "http://problem-service/api/problems/%s/testcase/manifest".formatted(problemId);
    }

    private void notifySubmission(Submission submission) {
        SimpMessagingTemplate template = messagingTemplate.getIfAvailable();
        if (template != null) {
            template.convertAndSend("/topic/submission/" + submission.getId(), toDto(submission));
        }
    }

    private void notifyContestRank(Long contestId) {
        SimpMessagingTemplate template = messagingTemplate.getIfAvailable();
        if (template != null) {
            template.convertAndSend("/topic/contest/" + contestId + "/rank", rankingService.contestRank(contestId));
        }
    }

    private void notifyGlobalRank() {
        SimpMessagingTemplate template = messagingTemplate.getIfAvailable();
        if (template != null) {
            template.convertAndSend("/topic/rank/global", rankingService.globalRank());
        }
    }

    private SubmissionDTO toSubmissionDto(Submission submission) {
        return SubmissionDTO.builder()
                .id(submission.getId())
                .userId(submission.getUserId())
                .problemId(submission.getProblemId())
                .contestId(submission.getContestId())
                .language(LanguageType.fromValue(submission.getLanguage()))
                .code(submission.getCode())
                .status(SubmitStatus.fromValue(submission.getStatus().name()))
                .score(submission.getScore())
                .timeUsedMs(submission.getTimeUsedMs())
                .memoryUsedKb(submission.getMemoryUsedKb())
                .judgeMessage(submission.getJudgeMessage())
                .judgedByWorker(submission.getJudgedByWorker())
                .submittedAt(submission.getSubmittedAt())
                .judgedAt(submission.getJudgedAt())
                .build();
    }

    private static int scoreFromCases(List<JudgeResult.CaseResult> cases) {
        if (cases == null || cases.isEmpty()) {
            return 0;
        }
        long accepted = cases.stream().filter(c -> "AC".equalsIgnoreCase(c.getStatus())).count();
        return (int) ((accepted * 100) / cases.size());
    }

    public record SubmitCommand(
            Long userId,
            Long problemId,
            Long contestId,
            String language,
            String code,
            Integer timeLimitMs,
            Integer memoryLimitMb,
            String testcaseManifestUrl) {
        public SubmitCommand(Long userId, Long problemId, Long contestId, String language, String code) {
            this(userId, problemId, contestId, language, code, null, null, null);
        }
    }
}
