package com.judgemesh.submit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.dto.SubmissionDTO;
import com.judgemesh.submit.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class SubmissionWebSocketHandler extends TextWebSocketHandler {

    private final SubmissionSocketHub hub;
    private final SubmissionService submissionService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        long submissionId = parseSubmissionId(session);
        hub.register(submissionId, session);
        SubmissionDTO dto = submissionService.getSubmission(submissionId);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(dto)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        Long submissionId = tryParseSubmissionId(session);
        if (submissionId != null) {
            hub.unregister(submissionId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        log.debug("ignore client message on submission websocket: {}", message.getPayload());
    }

    private Long tryParseSubmissionId(WebSocketSession session) {
        try {
            return parseSubmissionId(session);
        } catch (Exception ex) {
            return null;
        }
    }

    private long parseSubmissionId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        String[] segments = path.split("/");
        return Long.parseLong(segments[segments.length - 1]);
    }
}
