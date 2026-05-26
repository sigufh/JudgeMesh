package com.judgemesh.dispatcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.dispatcher.config.DispatcherProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JudgeTaskRetryServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DispatcherProperties properties = new DispatcherProperties();
    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final DispatcherMetrics dispatcherMetrics = mock(DispatcherMetrics.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider = mock(ObjectProvider.class);

    @Test
    void requeuesTaskAndIncrementsRetryCountBeforeMaxRetry() throws Exception {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        JudgeTaskRetryService service = new JudgeTaskRetryService(properties, objectMapper, rabbitTemplateProvider, dispatcherMetrics);
        JudgeTask task = JudgeTask.builder()
                .submitId(1001L)
                .retryCount(1)
                .build();

        service.retryOrDeadLetter(task, new IllegalStateException("worker failed"));

        org.mockito.ArgumentCaptor<String> payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("submit.queue"), payloadCaptor.capture());
        JudgeTask retried = objectMapper.readValue(payloadCaptor.getValue(), JudgeTask.class);
        assertThat(retried.getRetryCount()).isEqualTo(2);
        assertThat(retried.getSubmitId()).isEqualTo(1001L);
    }

    @Test
    void sendsTaskToDeadLetterQueueAtMaxRetry() throws Exception {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        JudgeTaskRetryService service = new JudgeTaskRetryService(properties, objectMapper, rabbitTemplateProvider, dispatcherMetrics);
        JudgeTask task = JudgeTask.builder()
                .submitId(1002L)
                .retryCount(3)
                .build();

        service.retryOrDeadLetter(task, new IllegalStateException("worker failed"));

        org.mockito.ArgumentCaptor<String> payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("submit.queue.dlq"), payloadCaptor.capture());
        JudgeTask deadLettered = objectMapper.readValue(payloadCaptor.getValue(), JudgeTask.class);
        assertThat(deadLettered.getRetryCount()).isEqualTo(3);
        assertThat(deadLettered.getSubmitId()).isEqualTo(1002L);
    }

    @Test
    void sendsInvalidPayloadToDeadLetterQueue() {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        JudgeTaskRetryService service = new JudgeTaskRetryService(properties, objectMapper, rabbitTemplateProvider, dispatcherMetrics);

        service.deadLetterRaw("{bad-json", new IllegalArgumentException("invalid"));

        verify(rabbitTemplate).convertAndSend("submit.queue.dlq", "{bad-json");
    }
}
