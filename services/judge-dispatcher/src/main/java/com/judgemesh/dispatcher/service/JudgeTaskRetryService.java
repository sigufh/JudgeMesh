package com.judgemesh.dispatcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.dispatcher.config.DispatcherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeTaskRetryService {

    private final DispatcherProperties properties;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final DispatcherMetrics dispatcherMetrics;

    public void retryOrDeadLetter(JudgeTask task, Throwable cause) {
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        if (retryCount >= properties.getWorker().getMaxRetry()) {
            publish(properties.getMq().getDeadLetterQueue(), task);
            dispatcherMetrics.recordRetry("dead_letter");
            log.warn("judge task moved to DLQ submitId={} retryCount={} reason={}",
                    task.getSubmitId(), retryCount, rootMessage(cause));
            return;
        }

        task.setRetryCount(retryCount + 1);
        publish(properties.getMq().getSubmitQueue(), task);
        dispatcherMetrics.recordRetry("requeue");
        log.info("judge task requeued submitId={} retryCount={} reason={}",
                task.getSubmitId(), task.getRetryCount(), rootMessage(cause));
    }

    public void deadLetterRaw(String payload, Throwable cause) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate unavailable, cannot publish invalid payload");
        }
        rabbitTemplate.convertAndSend(properties.getMq().getDeadLetterQueue(), payload);
        dispatcherMetrics.recordRetry("invalid_payload");
        log.warn("invalid judge task payload moved to DLQ reason={}", rootMessage(cause));
    }

    private void publish(String queue, JudgeTask task) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate unavailable, cannot publish retry task");
        }
        try {
            rabbitTemplate.convertAndSend(queue, objectMapper.writeValueAsString(task));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to publish judge task to " + queue, ex);
        }
    }

    private String rootMessage(Throwable cause) {
        if (cause == null) {
            return "unknown";
        }
        Throwable root = cause;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
