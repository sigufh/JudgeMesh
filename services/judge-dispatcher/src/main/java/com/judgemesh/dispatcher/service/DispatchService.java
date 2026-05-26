package com.judgemesh.dispatcher.service;

import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.dispatcher.config.DispatcherProperties;
import com.judgemesh.dispatcher.model.DispatchReceipt;
import com.judgemesh.dispatcher.model.DispatcherStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final DispatcherProperties properties;
    private final DispatcherLeaderService leaderService;
    private final WorkerSelectionService workerSelectionService;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ScheduledExecutorService scheduler;
    private final DispatcherMetrics dispatcherMetrics;

    private volatile Instant lastDispatchAt;

    public DispatchReceipt dispatch(JudgeTask task) {
        if (!leaderService.isLeader()) {
            throw new IllegalStateException("current dispatcher is not leader");
        }
        WorkerSelectionService.WorkerSelectionResult selection = workerSelectionService.selectWorker();
        String workerUrl = selection.workerUrl();
        workerSelectionService.noteDispatched(workerUrl);

        try {
            RestTemplate restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(3))
                    .setReadTimeout(Duration.ofSeconds(properties.getWorker().getTimeoutSeconds()))
                    .build();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    workerUrl + properties.getWorker().getJudgePath(),
                    task,
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("worker returned " + response.getStatusCode());
            }
            lastDispatchAt = Instant.now();
            scheduler.schedule(() -> workerSelectionService.noteReleased(workerUrl),
                    properties.getWorker().getTimeoutSeconds(), TimeUnit.SECONDS);
            dispatcherMetrics.recordDispatch(workerUrl, "success");
            return DispatchReceipt.builder()
                    .submissionId(task.getSubmitId())
                    .workerUrl(workerUrl)
                    .attempt(task.getRetryCount() == null ? 0 : task.getRetryCount() + 1)
                    .leaderId(leaderService.leaderId())
                    .dispatchedAt(lastDispatchAt)
                    .build();
        } catch (RuntimeException ex) {
            workerSelectionService.noteReleased(workerUrl);
            workerSelectionService.noteFailed(workerUrl);
            dispatcherMetrics.recordDispatch(workerUrl, "failure");
            throw ex;
        }
    }

    public DispatcherStatusDTO status() {
        List<String> workers = properties.getWorker().getEndpoints();
        return DispatcherStatusDTO.builder()
                .mode(leaderService.mode())
                .leader(leaderService.isLeader())
                .leaderId(leaderService.leaderId())
                .lastDispatchAt(lastDispatchAt)
                .workers(workers)
                .inflight(workerSelectionService.snapshotInflight())
                .blacklistedWorkers(workerSelectionService.snapshotBlacklist())
                .build();
    }

    public void stepDown() {
        leaderService.stepDown();
    }
}
