package com.judgemesh.submit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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
    Queue submitQueue(@Value("${judgemesh.mq.submit-queue:submit.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding submitBinding(Queue submitQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(submitQueue).to(judgeExchange).with("judge.task");
    }
}
