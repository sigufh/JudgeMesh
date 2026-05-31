package com.judgemesh.dispatcher.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class DispatcherMetrics {

    private final MeterRegistry registry;
    private final DispatcherLeaderService leaderService;

    @PostConstruct
    void registerGauges() {
        Gauge.builder("oj_dispatcher_is_leader", leaderService, service -> service.isLeader() ? 1.0 : 0.0)
                .description("Whether this dispatcher instance is the active leader")
                .register(registry);
    }

    public void recordDispatch(String workerUrl, String result) {
        registry.counter("oj_dispatch_total",
                "worker", sanitizeWorker(workerUrl),
                "result", result)
                .increment();
    }

    public void recordRetry(String outcome) {
        registry.counter("oj_dispatch_retry_total", "outcome", outcome).increment();
    }

    private String sanitizeWorker(String workerUrl) {
        return workerUrl == null || workerUrl.isBlank() ? "unknown" : workerUrl;
    }
}
