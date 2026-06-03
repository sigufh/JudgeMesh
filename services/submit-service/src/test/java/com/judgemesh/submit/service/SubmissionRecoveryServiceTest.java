package com.judgemesh.submit.service;

import com.judgemesh.api.client.ProblemClient;
import com.judgemesh.api.dto.ProblemDTO;
import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.submit.domain.Submission;
import com.judgemesh.submit.domain.SubmissionStatus;
import com.judgemesh.submit.repository.SubmissionStore;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SubmissionRecoveryServiceTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<JdbcTemplate> jdbcProvider = mock(ObjectProvider.class);
    private final SubmissionStore store = new SubmissionStore(jdbcProvider);
    private final ContestService contestService = mock(ContestService.class);
    private final RankingService rankingService = mock(RankingService.class);
    private final SubmitDedupService dedupService = mock(SubmitDedupService.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<ProblemClient> problemClientProvider = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider = mock(ObjectProvider.class);
    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final ProblemClient problemClient = mock(ProblemClient.class);
    private final SubmissionService submissionService = new SubmissionService(
            store,
            contestService,
            rankingService,
            dedupService,
            problemClientProvider,
            mock(ObjectProvider.class),
            rabbitTemplateProvider,
            messagingTemplateProvider,
            "judgemesh.exchange",
            "judge.task",
            "http://submit-service:8083/api/submit/internal/result",
            "http://problem-service:8082",
            false,
            1,
            65536,
            "http://dispatcher:8084",
            true);

    SubmissionRecoveryServiceTest() {
        when(jdbcProvider.getIfAvailable()).thenReturn(null);
        when(problemClientProvider.getIfAvailable()).thenReturn(problemClient);
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        when(messagingTemplateProvider.getIfAvailable()).thenReturn(null);
        when(problemClient.getById(6L)).thenReturn(ProblemDTO.builder()
                .id(6L)
                .title("reverse-string")
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .testcaseManifestUrl("http://problem-service/api/problem/internal/6/testcase/manifest")
                .build());
        when(problemClient.getManifest(6L)).thenReturn(List.of(
                JudgeTask.TestCaseRef.builder()
                        .name("1")
                        .inputUrl("http://minio/input/1")
                        .expectedOutputUrl("http://minio/output/1")
                        .build()));
    }

    @Test
    void requeuesTimedOutJudgingAndIncrementsRetryCount() {
        Instant oldJudgingStartedAt = Instant.now().minusSeconds(90);
        Submission submission = Submission.builder()
                .userId(2L)
                .problemId(6L)
                .language("PYTHON")
                .code("print(input()[::-1])\n")
                .codeLength(20)
                .status(SubmissionStatus.JUDGING)
                .score(0)
                .submittedAt(Instant.now().minusSeconds(120))
                .judgingStartedAt(oldJudgingStartedAt)
                .judgeRetryCount(0)
                .judgedByWorker("judge-worker-a")
                .build();
        store.save(submission);

        boolean handled = submissionService.recoverTimedOutJudging(
                submission.getId(),
                oldJudgingStartedAt,
                0,
                2);

        assertThat(handled).isTrue();
        Submission updated = store.findById(submission.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubmissionStatus.JUDGING);
        assertThat(updated.getJudgeRetryCount()).isEqualTo(1);
        assertThat(updated.getJudgedByWorker()).isNull();
        assertThat(updated.getJudgeMessage()).contains("auto retry 1/2");
        assertThat(updated.getJudgingStartedAt()).isAfter(oldJudgingStartedAt);
        assertThat(updated.getActiveAttemptId()).isNotBlank();

        org.mockito.ArgumentCaptor<JudgeTask> taskCaptor = org.mockito.ArgumentCaptor.forClass(JudgeTask.class);
        verify(rabbitTemplate).convertAndSend(eq("judgemesh.exchange"), eq("judge.task"), taskCaptor.capture());
        JudgeTask task = taskCaptor.getValue();
        assertThat(task.getSubmitId()).isEqualTo(submission.getId());
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getAttemptId()).isEqualTo(updated.getActiveAttemptId());
        assertThat(task.getLanguage()).isEqualTo("PYTHON");
        assertThat(task.getTestcases()).hasSize(1);
    }

    @Test
    void marksTimedOutJudgingAsSystemErrorAfterMaxRetry() {
        Instant oldJudgingStartedAt = Instant.now().minusSeconds(90);
        Submission submission = Submission.builder()
                .userId(2L)
                .problemId(6L)
                .language("PYTHON")
                .code("print(input()[::-1])\n")
                .codeLength(20)
                .status(SubmissionStatus.JUDGING)
                .score(0)
                .submittedAt(Instant.now().minusSeconds(120))
                .judgingStartedAt(oldJudgingStartedAt)
                .judgeRetryCount(2)
                .judgedByWorker("judge-worker-a")
                .build();
        store.save(submission);

        boolean handled = submissionService.recoverTimedOutJudging(
                submission.getId(),
                oldJudgingStartedAt,
                2,
                2);

        assertThat(handled).isTrue();
        Submission updated = store.findById(submission.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubmissionStatus.SE);
        assertThat(updated.getJudgeMessage()).contains("worker timeout after 2 auto retries");
        assertThat(updated.getJudgedAt()).isNotNull();
        assertThat(updated.getJudgingStartedAt()).isNull();
        assertThat(updated.getActiveAttemptId()).isNull();
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void ignoresStaleJudgeResultFromOldAttempt() {
        Submission submission = Submission.builder()
                .userId(2L)
                .problemId(6L)
                .language("PYTHON")
                .code("print(input()[::-1])\n")
                .codeLength(20)
                .status(SubmissionStatus.JUDGING)
                .score(0)
                .submittedAt(Instant.now().minusSeconds(120))
                .judgingStartedAt(Instant.now().minusSeconds(30))
                .judgeRetryCount(1)
                .judgedByWorker("judge-worker-new")
                .activeAttemptId("attempt-new")
                .build();
        store.save(submission);

        submissionService.applyResult(JudgeResult.builder()
                .submitId(submission.getId())
                .attemptId("attempt-old")
                .status("AC")
                .message("stale")
                .workerId("judge-worker-old")
                .workerVersion("test")
                .timeUsedMs(1)
                .memoryUsedKb(1)
                .build());

        Submission updated = store.findById(submission.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubmissionStatus.JUDGING);
        assertThat(updated.getActiveAttemptId()).isEqualTo("attempt-new");
        assertThat(updated.getJudgedByWorker()).isEqualTo("judge-worker-new");
        assertThat(updated.getJudgedAt()).isNull();
    }
}
