package com.judgemesh.submit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.submit.config.SubmitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeTaskPublisher {

    private final SubmitProperties properties;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    public void publish(JudgeTask task) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            log.info("RabbitTemplate unavailable, skip publishing JudgeTask {}", task.getSubmitId());
            return;
        }
        try {
            rabbitTemplate.convertAndSend(
                    properties.getMq().getQueue(),
                    objectMapper.writeValueAsString(task));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish JudgeTask", ex);
        }
    }
}
