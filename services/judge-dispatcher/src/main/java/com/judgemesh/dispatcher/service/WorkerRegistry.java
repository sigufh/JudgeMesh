package com.judgemesh.dispatcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WorkerRegistry {
    private final List<URI> workers;
    private final Map<URI, Integer> inflight = new ConcurrentHashMap<>();
    private final Map<URI, Instant> unavailableUntil = new ConcurrentHashMap<>();
    private final Map<URI, String> lastError = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final Duration blacklistDuration;
    private final AtomicInteger cursor = new AtomicInteger();

    public WorkerRegistry(
            @Value("${judgemesh.worker.urls:http://127.0.0.1:8090}") String workerUrls,
            @Value("${judgemesh.worker.blacklist-seconds:30}") long blacklistSeconds,
            RestTemplate restTemplate) {
        this.workers = Arrays.stream(workerUrls.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(URI::create)
                .toList();
        this.restTemplate = restTemplate;
        this.blacklistDuration = Duration.ofSeconds(Math.max(1, blacklistSeconds));
    }

    public URI acquire() {
        return acquire(Set.of());
    }

    public URI acquire(Set<URI> excluded) {
        if (workers.isEmpty()) {
            throw new IllegalStateException("no worker urls configured");
        }
        List<URI> healthyWorkers = workers.stream()
                .filter(uri -> !excluded.contains(uri))
                .filter(this::healthy)
                .toList();
        if (healthyWorkers.isEmpty()) {
            throw new IllegalStateException("no healthy worker available");
        }

        int minInflight = healthyWorkers.stream()
                .mapToInt(uri -> inflight.getOrDefault(uri, 0))
                .min()
                .orElse(0);
        List<URI> candidates = healthyWorkers.stream()
                .filter(uri -> inflight.getOrDefault(uri, 0) == minInflight)
                .toList();
        URI worker = candidates.get(Math.floorMod(cursor.getAndIncrement(), candidates.size()));
        inflight.merge(worker, 1, Integer::sum);
        return worker;
    }

    public void release(URI worker) {
        inflight.computeIfPresent(worker, (ignored, count) -> Math.max(0, count - 1));
    }

    public Map<String, Integer> inflightSnapshot() {
        return inflight.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }

    public Map<String, Object> status() {
        List<Map<String, Object>> snapshot = workers.stream()
                .map(this::describeWorker)
                .toList();
        long available = snapshot.stream()
                .filter(item -> Boolean.TRUE.equals(item.get("available")))
                .count();
        return Map.of(
                "configured", workers.stream().map(URI::toString).toList(),
                "availableCount", available,
                "totalCount", workers.size(),
                "workers", snapshot);
    }

    public void markFailed(URI worker) {
        markFailed(worker, "worker health probe failed");
    }

    public void markFailed(URI worker, String reason) {
        unavailableUntil.put(worker, Instant.now().plus(blacklistDuration));
        lastError.put(worker, reason);
    }

    public int configuredCount() {
        return workers.size();
    }

    private boolean healthy(URI worker) {
        Instant until = unavailableUntil.get(worker);
        if (until != null && until.isAfter(Instant.now())) {
            return false;
        }
        unavailableUntil.remove(worker);
        try {
            var response = restTemplate.getForEntity(worker.resolve("/health"), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                markFailed(worker, "health returned " + response.getStatusCode().value());
                return false;
            }
            lastError.remove(worker);
            return true;
        } catch (RestClientException ex) {
            markFailed(worker, ex.getMessage());
            return false;
        }
    }

    private Map<String, Object> describeWorker(URI worker) {
        Map<String, Object> workerView = new LinkedHashMap<>();
        workerView.put("url", worker.toString());
        workerView.put("inflight", inflight.getOrDefault(worker, 0));

        Instant until = unavailableUntil.get(worker);
        boolean blacklisted = until != null && until.isAfter(Instant.now());
        boolean available = !blacklisted && healthy(worker);
        until = unavailableUntil.get(worker);
        blacklisted = until != null && until.isAfter(Instant.now());
        String state = available ? "UP" : (blacklisted ? "BLACKLISTED" : "DOWN");

        workerView.put("available", available);
        workerView.put("state", state);
        workerView.put("blacklistedUntil", blacklisted ? until.toString() : null);
        workerView.put("lastError", lastError.get(worker));
        return workerView;
    }
}
