package com.judgemesh.submit.service;

import com.judgemesh.api.enumx.LanguageType;
import com.judgemesh.api.enumx.SubmitStatus;
import com.judgemesh.submit.repository.SubmitStateRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SubmitMetrics {

    private final MeterRegistry registry;
    private final SubmitStateRepository repository;

    @PostConstruct
    void registerGauges() {
        Gauge.builder("oj_submission_pending", repository, repo -> repo.countSubmissionsByStatus(SubmitStatus.PENDING))
                .description("Pending submissions currently stored by submit-service")
                .register(registry);
    }

    public void recordSubmission(LanguageType language, SubmitStatus status) {
        registry.counter(
                "oj_submission_total",
                "language", language == null ? "unknown" : language.toWireValue(),
                "status", status == null ? "unknown" : status.toWireValue())
                .increment();
    }

    public void recordJudgeResult(LanguageType language, SubmitStatus status, Duration latency) {
        recordSubmission(language, status);
        if (latency != null && !latency.isNegative()) {
            registry.timer("oj_submit_latency_seconds",
                    "language", language == null ? "unknown" : language.toWireValue(),
                    "status", status == null ? "unknown" : status.toWireValue())
                    .record(latency);
        }
    }
}
