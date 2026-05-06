package com.judgemesh.submit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.submit.service.ContestService;
import com.judgemesh.submit.service.SubmissionService;
import com.judgemesh.submit.websocket.ContestRankSocketHub;
import com.judgemesh.submit.websocket.ContestRankWebSocketHandler;
import com.judgemesh.submit.websocket.SubmissionSocketHub;
import com.judgemesh.submit.websocket.SubmissionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SubmissionSocketHub submissionSocketHub;
    private final ContestRankSocketHub contestRankSocketHub;
    private final SubmissionService submissionService;
    private final ContestService contestService;
    private final ObjectMapper objectMapper;
    private final SubmitProperties properties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        SubmissionWebSocketHandler submissionHandler =
                new SubmissionWebSocketHandler(submissionSocketHub, submissionService, objectMapper);
        ContestRankWebSocketHandler contestHandler =
                new ContestRankWebSocketHandler(contestRankSocketHub, contestService, objectMapper);

        String[] allowedOrigins = properties.getWebsocket().getAllowedOrigins().toArray(String[]::new);
        registry.addHandler(submissionHandler, "/ws/submission/*")
                .setAllowedOrigins(allowedOrigins);
        registry.addHandler(contestHandler, "/ws/contest/*/rank")
                .setAllowedOrigins(allowedOrigins);
    }
}
