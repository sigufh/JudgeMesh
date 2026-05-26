package com.judgemesh.dispatcher.messaging;

import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.dispatcher.service.DispatcherService.DispatchResult;
import com.judgemesh.dispatcher.service.DispatcherService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "judgemesh.dispatcher", name = "listener-enabled", havingValue = "true", matchIfMissing = true)
public class JudgeTaskListener {
    private final DispatcherService dispatcherService;
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String retryRoutingKey;
    private final String deadRoutingKey;
    private final int maxRetry;

    public JudgeTaskListener(
            DispatcherService dispatcherService,
            RabbitTemplate rabbitTemplate,
            @Value("${judgemesh.mq.submit-exchange:judgemesh.exchange}") String exchange,
            @Value("${judgemesh.mq.retry-routing-key:judge.retry}") String retryRoutingKey,
            @Value("${judgemesh.mq.dead-routing-key:judge.dead}") String deadRoutingKey,
            @Value("${judgemesh.worker.max-retry:3}") int maxRetry) {
        this.dispatcherService = dispatcherService;
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.retryRoutingKey = retryRoutingKey;
        this.deadRoutingKey = deadRoutingKey;
        this.maxRetry = maxRetry;
    }

    @RabbitListener(queues = "${judgemesh.mq.submit-queue:submit.queue}")
    public void onTask(JudgeTask task) {
        DispatchResult result = dispatcherService.dispatch(task);
        if (result.ok()) {
            return;
        }
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        if (retryCount < maxRetry) {
            rabbitTemplate.convertAndSend(exchange, retryRoutingKey, retry(task, retryCount + 1));
            return;
        }
        dispatcherService.reportSystemError(task,
                "dispatcher exhausted retry budget: " + nullSafe(result.message()));
        rabbitTemplate.convertAndSend(exchange, deadRoutingKey, task);
    }

    private static JudgeTask retry(JudgeTask task, int retryCount) {
        return JudgeTask.builder()
                .submitId(task.getSubmitId())
                .problemId(task.getProblemId())
                .source(task.getSource())
                .language(task.getLanguage())
                .timeLimitMs(task.getTimeLimitMs())
                .memoryLimitMb(task.getMemoryLimitMb())
                .testcaseManifestUrl(task.getTestcaseManifestUrl())
                .testcases(task.getTestcases())
                .callbackUrl(task.getCallbackUrl())
                .retryCount(retryCount)
                .build();
    }

    private static String nullSafe(String value) {
        return value == null ? "unknown" : value;
    }
}
