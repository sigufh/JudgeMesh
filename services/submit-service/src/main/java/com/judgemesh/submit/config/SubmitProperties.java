package com.judgemesh.submit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "judgemesh")
public class SubmitProperties {

    private final Mq mq = new Mq();
    private final Submission submission = new Submission();
    private final WebSocket websocket = new WebSocket();
    private final Leaderboard leaderboard = new Leaderboard();

    @Data
    public static class Mq {
        private String queue = "submit.queue";
    }

    @Data
    public static class Submission {
        private int codeLengthLimit = 65536;
        private String callbackUrl = "http://submit-service:8083/api/submit/internal/result";
        private boolean deductScoreEnabled = false;
        private int submitCost = 1;
    }

    @Data
    public static class WebSocket {
        private List<String> allowedOrigins = List.of("*");
    }

    @Data
    public static class Leaderboard {
        private String globalKey = "rank:global";
        private String contestKeyPrefix = "rank:contest:";
        private String contestPubSubPrefix = "pubsub:contest:";
    }
}
