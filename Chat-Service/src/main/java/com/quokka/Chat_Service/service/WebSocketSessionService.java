package com.quokka.Chat_Service.service;

import com.quokka.Chat_Service.entity.WebSocketSession;
import com.quokka.Chat_Service.repository.WebSocketSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketSessionService {
    WebSocketSessionRepository webSocketSessionRepository;

    public WebSocketSession create(WebSocketSession webSocketSession) {
        return webSocketSessionRepository.save(webSocketSession);
    }

    public void deleteBySocketSessionId(String socketSessionId) {
        webSocketSessionRepository.deleteBySocketSessionId(socketSessionId);
    }
}
