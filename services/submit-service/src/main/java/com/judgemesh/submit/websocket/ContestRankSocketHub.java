package com.judgemesh.submit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.dto.ContestRankDTO;
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
public class ContestRankSocketHub {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(long contestId, WebSocketSession session) {
        sessions.computeIfAbsent(contestId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(long contestId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(contestId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(contestId);
            }
        }
    }

    public void broadcast(long contestId, ContestRankDTO dto) {
        Set<WebSocketSession> set = sessions.get(contestId);
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
            log.warn("failed to broadcast contest rank {}", contestId, ex);
        }
    }
}
