package com.judgemesh.dispatcher.service;

import com.judgemesh.dispatcher.config.DispatcherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerSelectionService {

    private final DispatcherProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ConcurrentHashMap<String, AtomicInteger> inflight = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> blacklistedUntil = new ConcurrentHashMap<>();

    public WorkerSelectionResult selectWorker() {
        List<String> candidates = new ArrayList<>(properties.getWorker().getEndpoints());
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No worker endpoints configured");
        }

        candidates.removeIf(this::isBlacklisted);
        candidates.sort(Comparator.comparingInt(this::inflightCount));
        for (String workerUrl : candidates) {
            if (isHealthy(workerUrl)) {
                return new WorkerSelectionResult(workerUrl, inflightCount(workerUrl));
            }
            noteFailed(workerUrl);
        }
        throw new IllegalStateException("No healthy worker available");
    }

    public void noteDispatched(String workerUrl) {
        inflight.computeIfAbsent(workerUrl, key -> new AtomicInteger()).incrementAndGet();
    }

    public void noteReleased(String workerUrl) {
        inflight.computeIfPresent(workerUrl, (key, counter) -> {
            counter.decrementAndGet();
            return counter.get() <= 0 ? null : counter;
        });
    }

    public void noteFailed(String workerUrl) {
        int blacklistSeconds = properties.getWorker().getBlacklistSeconds();
        if (blacklistSeconds <= 0) {
            return;
        }
        blacklistedUntil.put(workerUrl, Instant.now().plusSeconds(blacklistSeconds));
    }

    public Map<String, Integer> snapshotInflight() {
        return inflight.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    public Map<String, Instant> snapshotBlacklist() {
        purgeExpiredBlacklist();
        return Map.copyOf(blacklistedUntil);
    }

    private int inflightCount(String workerUrl) {
        AtomicInteger counter = inflight.get(workerUrl);
        return counter == null ? 0 : Math.max(0, counter.get());
    }

    private boolean isHealthy(String workerUrl) {
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
        try {
            String healthUrl = workerUrl + properties.getWorker().getHealthPath();
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.debug("worker health check failed: {}", workerUrl, ex);
            return false;
        }
    }

    private boolean isBlacklisted(String workerUrl) {
        purgeExpiredBlacklist();
        Instant until = blacklistedUntil.get(workerUrl);
        return until != null && until.isAfter(Instant.now());
    }

    private void purgeExpiredBlacklist() {
        Instant now = Instant.now();
        blacklistedUntil.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }

    public record WorkerSelectionResult(String workerUrl, int inflightCount) {
    }
}
