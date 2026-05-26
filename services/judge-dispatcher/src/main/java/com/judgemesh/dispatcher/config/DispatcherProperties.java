package com.judgemesh.dispatcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "judgemesh.dispatcher")
public class DispatcherProperties {

    private String mode = "memory";
    private final Etcd etcd = new Etcd();
    private final Worker worker = new Worker();
    private final Mq mq = new Mq();
    private final Admin admin = new Admin();

    @Data
    public static class Etcd {
        private String leaderKey = "/judgemesh/dispatcher/leader";
        private int leaseTtlSeconds = 10;
        private String endpoints = "http://127.0.0.1:2379";
        private String memberPrefix = "/judgemesh/dispatcher/members/";
    }

    @Data
    public static class Worker {
        private int timeoutSeconds = 30;
        private int maxRetry = 3;
        private int blacklistSeconds = 30;
        private List<String> endpoints = List.of("http://judge-worker:8090");
        private String healthPath = "/health";
        private String judgePath = "/judge";
    }

    @Data
    public static class Mq {
        private String submitQueue = "submit.queue";
        private String deadLetterQueue = "submit.queue.dlq";
    }

    @Data
    public static class Admin {
        private String statusPath = "/admin/dispatcher/status";
        private String killSelfPath = "/admin/dispatcher/chaos/kill-self";
    }
}
