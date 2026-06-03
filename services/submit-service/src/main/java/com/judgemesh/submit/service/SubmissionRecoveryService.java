package com.judgemesh.submit.service;

import com.judgemesh.submit.domain.Submission;
import com.judgemesh.submit.repository.SubmissionStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class SubmissionRecoveryService {
    private final SubmissionStore store;
    private final SubmissionService submissionService;
    private final boolean enabled;
    private final int judgingTimeoutSeconds;
    private final int batchSize;
    private final int maxRetry;

    public SubmissionRecoveryService(
            SubmissionStore store,
            SubmissionService submissionService,
            @Value("${judgemesh.submit.recovery.enabled:true}") boolean enabled,
            @Value("${judgemesh.submit.recovery.judging-timeout-seconds:30}") int judgingTimeoutSeconds,
            @Value("${judgemesh.submit.recovery.batch-size:20}") int batchSize,
            @Value("${judgemesh.submit.recovery.max-retry:2}") int maxRetry) {
        this.store = store;
        this.submissionService = submissionService;
        this.enabled = enabled;
        this.judgingTimeoutSeconds = judgingTimeoutSeconds;
        this.batchSize = batchSize;
        this.maxRetry = maxRetry;
    }

    @Scheduled(fixedDelayString = "${judgemesh.submit.recovery.scan-delay-ms:5000}")
    public void recoverTimedOutJudging() {
        if (!enabled) {
            return;
        }
        Instant cutoff = Instant.now().minusSeconds(judgingTimeoutSeconds);
        List<Submission> stale = store.findTimedOutJudging(cutoff, batchSize);
        if (stale.isEmpty()) {
            return;
        }

        int recovered = 0;
        int failed = 0;
        for (Submission submission : stale) {
            boolean handled = submissionService.recoverTimedOutJudging(
                    submission.getId(),
                    submission.getJudgingStartedAt(),
                    submission.getJudgeRetryCount() == null ? 0 : submission.getJudgeRetryCount(),
                    maxRetry);
            if (handled) {
                if ((submission.getJudgeRetryCount() == null ? 0 : submission.getJudgeRetryCount()) >= maxRetry) {
                    failed++;
                } else {
                    recovered++;
                }
            }
        }
        if (recovered > 0 || failed > 0) {
            log.warn("submission recovery handled stale judging tasks: recovered={}, failed={}, cutoff={}",
                    recovered, failed, cutoff);
        }
    }
}
