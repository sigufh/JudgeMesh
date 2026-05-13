package com.judgemesh.submit.service;

import com.judgemesh.api.client.ProblemClient;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.dto.SubmitDTO;
import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.submit.domain.Contest;
import com.judgemesh.submit.domain.Submission;
import com.judgemesh.submit.domain.SubmissionStatus;
import com.judgemesh.submit.repository.SubmissionStore;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubmissionService {
    private final SubmissionStore store;
    private final ContestService contestService;
    private final RankingService rankingService;
    private final ObjectProvider<ProblemClient> problemClient;
    private final ObjectProvider<RabbitTemplate> rabbitTemplate;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplate;
    private final Map<Long, Instant> lastSubmitAt = new ConcurrentHashMap<>();
    private final String exchange;
    private final String submitRoutingKey;
    private final String callbackUrl;
    private final String problemBaseUrl;
    private final RestTemplate directProblemClient = new RestTemplate();

    public SubmissionService(
            SubmissionStore store,
            ContestService contestService,
            RankingService rankingService,
            ObjectProvider<ProblemClient> problemClient,
            ObjectProvider<RabbitTemplate> rabbitTemplate,
            ObjectProvider<SimpMessagingTemplate> messagingTemplate,
            @Value("${judgemesh.mq.submit-exchange:judgemesh.exchange}") String exchange,
            @Value("${judgemesh.mq.submit-routing-key:judge.task}") String submitRoutingKey,
            @Value("${judgemesh.submit.callback-url:http://submit-service:8083/api/submit/internal/result}") String callbackUrl,
            @Value("${judgemesh.problem.base-url:http://127.0.0.1:8082}") String problemBaseUrl) {
        this.store = store;
        this.contestService = contestService;
        this.rankingService = rankingService;
        this.problemClient = problemClient;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
        this.exchange = exchange;
        this.submitRoutingKey = submitRoutingKey;
        this.callbackUrl = callbackUrl;
        this.problemBaseUrl = problemBaseUrl;
    }

    public SubmitDTO submit(SubmitCommand command) {
        Instant now = Instant.now();
        Instant previous = lastSubmitAt.put(command.userId(), now);
        if (previous != null && previous.plusSeconds(1).isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "submit too frequently");
        }
        if (command.contestId() != null) {
            Contest contest = contestService.get(command.contestId());
            if (!contest.getRegisteredUserIds().contains(command.userId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "contest not registered");
            }
            if (now.isBefore(contest.getStartTime()) || now.isAfter(contest.getEndTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contest is not active");
            }
        }

        Submission submission = Submission.builder()
                .userId(command.userId())
                .problemId(command.problemId())
                .contestId(command.contestId())
                .language(command.language().toUpperCase(Locale.ROOT))
                .code(command.code())
                .codeLength(command.code().length())
                .status(SubmissionStatus.PENDING)
                .score(0)
                .submittedAt(now)
                .build();
        store.save(submission);
        publishTask(submission);
        notifySubmission(submission);
        return toDto(submission);
    }

    public SubmitDTO get(Long id) {
        return toDto(find(id));
    }

    public List<SubmitDTO> mine(Long userId) {
        return store.findByUser(userId).stream().map(this::toDto).toList();
    }

    public SubmitDTO applyResult(JudgeResult result) {
        Submission submission = find(result.getSubmitId());
        SubmissionStatus status = SubmissionStatus.valueOf(result.getStatus().toUpperCase(Locale.ROOT));
        submission.setStatus(status);
        submission.setScore("AC".equals(status.name()) ? 100 : scoreFromCases(result.getCases()));
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
        }
        notifySubmission(submission);
        if (submission.getContestId() != null) {
            notifyContestRank(submission.getContestId());
        }
        return toDto(submission);
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

    private Submission find(Long id) {
        return store.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "submission not found"));
    }

    private void publishTask(Submission submission) {
        ProblemDTO problem = loadProblem(submission.getProblemId());
        List<JudgeTask.TestCaseRef> testcases = loadManifest(submission.getProblemId());
        JudgeTask task = JudgeTask.builder()
                .submitId(submission.getId())
                .problemId(submission.getProblemId())
                .source(submission.getCode())
                .language(submission.getLanguage().toLowerCase(Locale.ROOT))
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
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

    private ProblemDTO loadProblem(Long problemId) {
        try {
            ProblemClient client = problemClient.getIfAvailable();
            if (client != null) {
                return client.getById(problemId);
            }
        } catch (RuntimeException ignored) {
            // Fall through to local defaults so direct service demos still work.
        }
        return ProblemDTO.builder()
                .id(problemId)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .title("problem-" + problemId)
                .build();
    }

    private List<JudgeTask.TestCaseRef> loadManifest(Long problemId) {
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

    private static int scoreFromCases(List<JudgeResult.CaseResult> cases) {
        if (cases == null || cases.isEmpty()) {
            return 0;
        }
        long accepted = cases.stream().filter(c -> "AC".equalsIgnoreCase(c.getStatus())).count();
        return (int) ((accepted * 100) / cases.size());
    }

    public record SubmitCommand(Long userId, Long problemId, Long contestId, String language, String code) {
    }
}
