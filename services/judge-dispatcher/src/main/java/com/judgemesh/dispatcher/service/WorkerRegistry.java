package com.judgemesh.dispatcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WorkerRegistry {
    private static final String STATE_UP = "UP";
    private static final String STATE_BLACKLISTED = "BLACKLISTED";
    private static final String STATE_DOWN = "DOWN";
    private static final String STATE_SATURATED = "SATURATED";

    private final List<URI> configuredWorkers;
    private final Map<URI, Integer> inflight = new ConcurrentHashMap<>();
    private final Map<URI, Instant> unavailableUntil = new ConcurrentHashMap<>();
    private final Map<URI, String> lastError = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final Duration blacklistDuration;
    private final boolean discoveryEnabled;
    private final Duration discoveryRefreshInterval;
    private final AtomicInteger cursor = new AtomicInteger();
    private volatile List<URI> discoveredWorkers = List.of();
    private volatile Instant discoveredAt = Instant.EPOCH;

    public WorkerRegistry(
            @Value("${judgemesh.worker.urls:http://127.0.0.1:8090}") String workerUrls,
            @Value("${judgemesh.worker.blacklist-seconds:30}") long blacklistSeconds,
            @Value("${judgemesh.worker.discovery-enabled:true}") boolean discoveryEnabled,
            @Value("${judgemesh.worker.discovery-refresh-seconds:15}") long discoveryRefreshSeconds,
            RestTemplate restTemplate) {
        this.configuredWorkers = Arrays.stream(workerUrls.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(URI::create)
                .toList();
        this.restTemplate = restTemplate;
        this.blacklistDuration = Duration.ofSeconds(Math.max(1, blacklistSeconds));
        this.discoveryEnabled = discoveryEnabled;
        this.discoveryRefreshInterval = Duration.ofSeconds(Math.max(1, discoveryRefreshSeconds));
    }

    public URI acquire() {
        return acquire(Set.of());
    }

    public URI acquire(Set<URI> excluded) {
        List<URI> workers = workers();
        if (workers.isEmpty()) {
            throw new IllegalStateException("no worker urls configured");
        }
        List<WorkerCandidate> healthyWorkers = workers.stream()
                .filter(uri -> !excluded.contains(uri))
                .map(this::candidateFor)
                .filter(candidate -> candidate.probe().available())
                .toList();
        if (healthyWorkers.isEmpty()) {
            throw new IllegalStateException("no healthy worker available");
        }

        int minInflight = healthyWorkers.stream()
                .mapToInt(WorkerCandidate::effectiveInflight)
                .min()
                .orElse(0);
        List<URI> candidates = healthyWorkers.stream()
                .filter(candidate -> candidate.effectiveInflight() == minInflight)
                .map(WorkerCandidate::uri)
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
        List<URI> workers = workers();
        List<Map<String, Object>> snapshot = workers.stream()
                .map(this::describeWorker)
                .toList();
        long available = snapshot.stream()
                .filter(item -> Boolean.TRUE.equals(item.get("available")))
                .count();
        return Map.of(
                "configured", configuredWorkers.stream().map(URI::toString).toList(),
                "discovered", workers.stream().map(URI::toString).toList(),
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
        return workers().size();
    }

    private List<URI> workers() {
        if (!discoveryEnabled) {
            return configuredWorkers;
        }
        Instant now = Instant.now();
        if (!discoveredWorkers.isEmpty() && discoveredAt.plus(discoveryRefreshInterval).isAfter(now)) {
            return discoveredWorkers;
        }
        synchronized (this) {
            now = Instant.now();
            if (!discoveredWorkers.isEmpty() && discoveredAt.plus(discoveryRefreshInterval).isAfter(now)) {
                return discoveredWorkers;
            }
            List<URI> resolved = resolveConfiguredWorkers();
            discoveredWorkers = resolved;
            discoveredAt = now;
            cleanupStaleWorkers(resolved);
            return resolved;
        }
    }

    private List<URI> resolveConfiguredWorkers() {
        LinkedHashSet<URI> resolved = new LinkedHashSet<>();
        for (URI configuredWorker : configuredWorkers) {
            resolved.addAll(resolveWorker(configuredWorker));
        }
        if (resolved.isEmpty()) {
            return configuredWorkers;
        }
        return List.copyOf(resolved);
    }

    private List<URI> resolveWorker(URI configuredWorker) {
        if (!discoveryEnabled || configuredWorker.getHost() == null || configuredWorker.getHost().isBlank()) {
            return List.of(configuredWorker);
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(configuredWorker.getHost());
            LinkedHashSet<URI> resolved = new LinkedHashSet<>();
            for (InetAddress address : addresses) {
                resolved.add(withHost(configuredWorker, address.getHostAddress()));
            }
            return resolved.isEmpty() ? List.of(configuredWorker) : List.copyOf(resolved);
        } catch (UnknownHostException ex) {
            lastError.put(configuredWorker, "dns resolve failed: " + ex.getMessage());
            return List.of(configuredWorker);
        }
    }

    private static URI withHost(URI base, String host) {
        return URI.create(base.getScheme() + "://" + host + (base.getPort() > 0 ? ":" + base.getPort() : "")
                + (base.getPath() == null ? "" : base.getPath()));
    }

    private void cleanupStaleWorkers(List<URI> resolved) {
        Set<URI> active = Set.copyOf(resolved);
        inflight.keySet().removeIf(uri -> !active.contains(uri));
        unavailableUntil.keySet().removeIf(uri -> !active.contains(uri));
        lastError.keySet().removeIf(uri -> !active.contains(uri) && !configuredWorkers.contains(uri));
    }

    private WorkerProbe probe(URI worker) {
        Instant until = unavailableUntil.get(worker);
        if (until != null && until.isAfter(Instant.now())) {
            return new WorkerProbe(false, STATE_BLACKLISTED, inflight.getOrDefault(worker, 0), 0, lastError.get(worker));
        }
        unavailableUntil.remove(worker);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(worker.resolve("/health"), Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                markFailed(worker, "health returned " + response.getStatusCode().value());
                return new WorkerProbe(false, STATE_DOWN, inflight.getOrDefault(worker, 0), 0, lastError.get(worker));
            }
            Map<?, ?> payload = response.getBody();
            int observedInflight = asInt(payload == null ? null : payload.get("inflight"), inflight.getOrDefault(worker, 0));
            int maxConcurrency = asInt(payload == null ? null : payload.get("maxConcurrency"), 0);
            String reportedStatus = asString(payload == null ? null : payload.get("status"));
            if (STATE_SATURATED.equalsIgnoreCase(reportedStatus)
                    || (maxConcurrency > 0 && observedInflight >= maxConcurrency)) {
                lastError.put(worker, "worker saturated " + observedInflight + "/" + maxConcurrency);
                return new WorkerProbe(false, STATE_SATURATED, observedInflight, maxConcurrency, lastError.get(worker));
            }
            if (reportedStatus != null && !STATE_UP.equalsIgnoreCase(reportedStatus)) {
                markFailed(worker, "health reported " + reportedStatus);
                return new WorkerProbe(false, STATE_DOWN, observedInflight, maxConcurrency, lastError.get(worker));
            }
            lastError.remove(worker);
            return new WorkerProbe(true, STATE_UP, observedInflight, maxConcurrency, null);
        } catch (RestClientException ex) {
            markFailed(worker, ex.getMessage());
            return new WorkerProbe(false, STATE_DOWN, inflight.getOrDefault(worker, 0), 0, lastError.get(worker));
        }
    }

    private Map<String, Object> describeWorker(URI worker) {
        Map<String, Object> workerView = new LinkedHashMap<>();
        WorkerProbe probe = probe(worker);
        Instant until = unavailableUntil.get(worker);
        boolean blacklisted = until != null && until.isAfter(Instant.now());

        workerView.put("url", worker.toString());
        workerView.put("inflight", probe.inflight());
        workerView.put("maxConcurrency", probe.maxConcurrency());
        workerView.put("available", probe.available());
        workerView.put("state", probe.state());
        workerView.put("blacklistedUntil", blacklisted ? until.toString() : null);
        workerView.put("lastError", probe.error());
        return workerView;
    }

    private int effectiveInflight(URI worker, WorkerProbe probe) {
        return Math.max(inflight.getOrDefault(worker, 0), probe.inflight());
    }

    private WorkerCandidate candidateFor(URI worker) {
        WorkerProbe probe = probe(worker);
        return new WorkerCandidate(worker, probe, effectiveInflight(worker, probe));
    }

    private static int asInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    private static String asString(Object value) {
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    private record WorkerProbe(boolean available, String state, int inflight, int maxConcurrency, String error) {
    }

    private record WorkerCandidate(URI uri, WorkerProbe probe, int effectiveInflight) {
    }
}
