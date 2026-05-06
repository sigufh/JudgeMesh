package com.judgemesh.submit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judgemesh.api.dto.ContestRankDTO;
import com.judgemesh.submit.service.ContestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@RequiredArgsConstructor
public class ContestRankWebSocketHandler extends TextWebSocketHandler {

    private final ContestRankSocketHub hub;
    private final ContestService contestService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        long contestId = parseContestId(session);
        hub.register(contestId, session);
        ContestRankDTO dto = contestService.contestRank(contestId);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(dto)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        Long contestId = tryParseContestId(session);
        if (contestId != null) {
            hub.unregister(contestId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("ignore client message on contest websocket: {}", message.getPayload());
    }

    private Long tryParseContestId(WebSocketSession session) {
        try {
            return parseContestId(session);
        } catch (Exception ex) {
            return null;
        }
    }

    private long parseContestId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        String[] segments = path.split("/");
        return Long.parseLong(segments[segments.length - 2]);
    }
}
