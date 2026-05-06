package com.judgemesh.dispatcher.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.message.JudgeTask;
import com.judgemesh.dispatcher.config.DispatcherProperties;
import com.judgemesh.dispatcher.service.DispatchService;
import com.judgemesh.dispatcher.service.DispatcherLeaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeTaskListener {

    private final ObjectMapper objectMapper;
    private final DispatchService dispatchService;
    private final DispatcherLeaderService leaderService;
    private final DispatcherProperties properties;

    @RabbitListener(queues = "${judgemesh.dispatcher.mq.submitQueue:submit.queue}")
    public void onMessage(String payload) {
        if (!leaderService.isLeader()) {
            throw new IllegalStateException("follower dispatcher should not consume judge task");
        }
        try {
            JudgeTask task = objectMapper.readValue(payload, JudgeTask.class);
            dispatchService.dispatch(task);
        } catch (Exception ex) {
            log.warn("failed to dispatch judge task payload={}", payload, ex);
            throw new IllegalStateException("dispatch failed", ex);
        }
    }
}
