package com.judgemesh.dispatcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkerRegistry {
    private final List<URI> workers;
    private final Map<URI, Integer> inflight = new ConcurrentHashMap<>();
    private final Map<URI, Instant> unavailableUntil = new ConcurrentHashMap<>();
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
        if (workers.isEmpty()) {
            throw new IllegalStateException("no worker urls configured");
        }
        List<URI> healthyWorkers = workers.stream()
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
                .collect(java.util.stream.Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }

    public Map<String, Object> status() {
        return Map.of(
                "inflight", inflightSnapshot(),
                "unavailableUntil", unavailableUntil.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())),
                "configured", workers.stream().map(URI::toString).toList());
    }

    public void markFailed(URI worker) {
        unavailableUntil.put(worker, Instant.now().plus(blacklistDuration));
    }

    private boolean healthy(URI worker) {
        Instant until = unavailableUntil.get(worker);
        if (until != null && until.isAfter(Instant.now())) {
            return false;
        }
        unavailableUntil.remove(worker);
        try {
            restTemplate.getForEntity(worker.resolve("/health"), String.class);
            return true;
        } catch (RestClientException ex) {
            markFailed(worker);
            return false;
        }
    }
}
