package com.judgemesh.dispatcher.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    @Bean
    DirectExchange judgeExchange(@Value("${judgemesh.mq.submit-exchange:judgemesh.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    Queue submitQueue(
            @Value("${judgemesh.mq.submit-queue:submit.queue}") String queueName,
            @Value("${judgemesh.mq.submit-exchange:judgemesh.exchange}") String exchange,
            @Value("${judgemesh.mq.dead-routing-key:judge.dead}") String deadRoutingKey) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(deadRoutingKey)
                .build();
    }

    @Bean
    Queue submitRetryQueue(
            @Value("${judgemesh.mq.retry-queue:submit.retry.queue}") String queueName,
            @Value("${judgemesh.mq.submit-exchange:judgemesh.exchange}") String exchange,
            @Value("${judgemesh.mq.submit-routing-key:judge.task}") String submitRoutingKey,
            @Value("${judgemesh.mq.retry-delay-ms:5000}") int retryDelayMs) {
        return QueueBuilder.durable(queueName)
                .ttl(retryDelayMs)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(submitRoutingKey)
                .build();
    }

    @Bean
    Queue submitDeadQueue(@Value("${judgemesh.mq.dead-queue:submit.dlq}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding submitBinding(
            @Qualifier("submitQueue") Queue submitQueue,
            DirectExchange judgeExchange,
            @Value("${judgemesh.mq.submit-routing-key:judge.task}") String routingKey) {
        return BindingBuilder.bind(submitQueue).to(judgeExchange).with(routingKey);
    }

    @Bean
    Binding submitRetryBinding(
            @Qualifier("submitRetryQueue") Queue submitRetryQueue,
            DirectExchange judgeExchange,
            @Value("${judgemesh.mq.retry-routing-key:judge.retry}") String routingKey) {
        return BindingBuilder.bind(submitRetryQueue).to(judgeExchange).with(routingKey);
    }

    @Bean
    Binding submitDeadBinding(
            @Qualifier("submitDeadQueue") Queue submitDeadQueue,
            DirectExchange judgeExchange,
            @Value("${judgemesh.mq.dead-routing-key:judge.dead}") String routingKey) {
        return BindingBuilder.bind(submitDeadQueue).to(judgeExchange).with(routingKey);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
