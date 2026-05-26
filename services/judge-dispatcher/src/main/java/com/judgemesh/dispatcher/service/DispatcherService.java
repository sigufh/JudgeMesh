package com.judgemesh.dispatcher.service;

import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.api.message.JudgeTask;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class DispatcherService {
    private final LeaderElectionService leaderElectionService;
    private final WorkerRegistry workerRegistry;
    private final RestTemplate restTemplate;

    public DispatcherService(
            LeaderElectionService leaderElectionService,
            WorkerRegistry workerRegistry,
            RestTemplate restTemplate) {
        this.leaderElectionService = leaderElectionService;
        this.workerRegistry = workerRegistry;
        this.restTemplate = restTemplate;
    }

    public DispatchResult dispatch(JudgeTask task) {
        if (!leaderElectionService.isLeader()) {
            return new DispatchResult(false, null, "not leader");
        }

        Set<URI> attempted = new LinkedHashSet<>();
        DispatchResult lastFailure = null;
        while (attempted.size() < workerRegistry.configuredCount()) {
            URI worker;
            try {
                worker = workerRegistry.acquire(attempted);
            } catch (IllegalStateException ex) {
                return lastFailure != null ? lastFailure : new DispatchResult(false, null, ex.getMessage());
            }
            attempted.add(worker);
            try {
                markJudging(task, worker);
                URI endpoint = worker.resolve("/judge");
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, task, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return new DispatchResult(true, worker.toString(), "accepted");
                }
                String message = "worker rejected task with status " + response.getStatusCode().value();
                workerRegistry.markFailed(worker, message);
                lastFailure = new DispatchResult(false, worker.toString(), message);
            } catch (RestClientException ex) {
                workerRegistry.markFailed(worker, ex.getMessage());
                lastFailure = new DispatchResult(false, worker.toString(), ex.getMessage());
            } finally {
                workerRegistry.release(worker);
            }
        }
        return lastFailure != null ? lastFailure : new DispatchResult(false, null, "no healthy worker available");
    }

    public Map<String, Object> status() {
        return Map.of(
                "leader", leaderElectionService.status(),
                "workers", workerRegistry.status());
    }

    private void markJudging(JudgeTask task, URI worker) {
        if (task.getCallbackUrl() == null || task.getCallbackUrl().isBlank()) {
            return;
        }
        String callback = task.getCallbackUrl().replace("/result", "/" + task.getSubmitId() + "/judging");
        try {
            restTemplate.postForEntity(callback + "?workerId=" + worker.getAuthority(), null, String.class);
        } catch (RestClientException ignored) {
            // The worker callback remains the source of truth; marking judging is best effort.
        }
    }

    public void reportSystemError(JudgeTask task, String message) {
        if (task.getCallbackUrl() == null || task.getCallbackUrl().isBlank()) {
            return;
        }
        JudgeResult result = JudgeResult.builder()
                .submitId(task.getSubmitId())
                .status("SE")
                .message("dispatcher failed to dispatch: " + message)
                .workerId("dispatcher")
                .workerVersion("0.0.1-SNAPSHOT")
                .timeUsedMs(0)
                .memoryUsedKb(0)
                .build();
        try {
            restTemplate.postForEntity(task.getCallbackUrl(), result, String.class);
        } catch (RestClientException ignored) {
            // No further local recovery path; RabbitMQ redelivery policy handles production retries.
        }
    }

    public record DispatchResult(boolean ok, String worker, String message) {
    }
}
