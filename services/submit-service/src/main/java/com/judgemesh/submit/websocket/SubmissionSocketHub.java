package com.judgemesh.submit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.dto.SubmissionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionSocketHub {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(long submissionId, WebSocketSession session) {
        sessions.computeIfAbsent(submissionId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(long submissionId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(submissionId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(submissionId);
            }
        }
    }

    public void broadcast(long submissionId, SubmissionDTO dto) {
        Set<WebSocketSession> set = sessions.get(submissionId);
        if (set == null || set.isEmpty()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(dto);
            for (WebSocketSession session : set) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException ex) {
            log.warn("failed to broadcast submission {}", submissionId, ex);
        }
    }
}
