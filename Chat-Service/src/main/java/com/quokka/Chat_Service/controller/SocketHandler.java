package com.quokka.Chat_Service.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.quokka.Chat_Service.dto.request.IntrospectRequest;
import com.quokka.Chat_Service.entity.WebSocketSession;
import com.quokka.Chat_Service.service.JobmateService;
import com.quokka.Chat_Service.service.WebSocketSessionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SocketHandler {
    SocketIOServer socketIOServer;
    JobmateService jobmateService;
    WebSocketSessionService webSocketSessionService;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        var introspectResponse = jobmateService.introspect(IntrospectRequest.builder().token(token).build());

        if (!introspectResponse.isValid()) {
            log.error("Invalid token for client: {}", client.getSessionId());
            client.disconnect();
            return;
        }

        log.info("Client {} authenticated, userId: {}", client.getSessionId(), introspectResponse.getUserId());

        // Save session
        WebSocketSession session = WebSocketSession.builder()
                .socketSessionId(client.getSessionId().toString())
                .userId(introspectResponse.getUserId())
                .createdDate(Instant.now())
                .build();
        webSocketSessionService.create(session);
    }

    // Join conversation room (frontend emit "joinRoom", roomId = conversationId)
    @OnEvent("joinRoom")
    public void onJoinRoom(SocketIOClient client, String conversationId) {
        client.joinRoom(conversationId);
        log.info("Client {} joined room {}", client.getSessionId(), conversationId);
    }

    // Leave room when user closes conversation
    @OnEvent("leaveRoom")
    public void onLeaveRoom(SocketIOClient client, String conversationId) {
        client.leaveRoom(conversationId);
        log.info("Client {} left room {}", client.getSessionId(), conversationId);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info(" Client disconnected: {}", client.getSessionId());
        webSocketSessionService.deleteBySocketSessionId(client.getSessionId().toString());
    }

    @PostConstruct
    public void startServer() {
        socketIOServer.addListeners(this);
        socketIOServer.start();
        log.info(" Socket.IO server started on port {}", socketIOServer.getConfiguration().getPort());
    }

    @PreDestroy
    public void stopServer() {
        socketIOServer.stop();
        log.info(" Socket.IO server stopped");
    }
}
