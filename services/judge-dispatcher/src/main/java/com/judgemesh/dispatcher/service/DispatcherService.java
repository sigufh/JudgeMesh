package com.judgemesh.dispatcher.service;

import com.judgemesh.api.message.JudgeResult;
import com.judgemesh.api.message.JudgeTask;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

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
        URI worker;
        try {
            worker = workerRegistry.acquire();
        } catch (IllegalStateException ex) {
            return new DispatchResult(false, null, ex.getMessage());
        }
        try {
            markJudging(task, worker);
            URI endpoint = worker.resolve("/judge");
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, task, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                workerRegistry.markFailed(worker);
                return new DispatchResult(false, worker.toString(), "worker rejected task");
            }
            return new DispatchResult(true, worker.toString(), "accepted");
        } catch (RestClientException ex) {
            workerRegistry.markFailed(worker);
            return new DispatchResult(false, worker.toString(), ex.getMessage());
        } finally {
            workerRegistry.release(worker);
        }
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
            restTemplate.postForEntity(callback + "?workerId=" + worker.getHost(), null, String.class);
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
