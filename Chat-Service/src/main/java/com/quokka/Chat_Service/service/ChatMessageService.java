package com.quokka.Chat_Service.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quokka.Chat_Service.dto.request.ChatMessageRequest;
import com.quokka.Chat_Service.dto.response.ChatMessageResponse;
import com.quokka.Chat_Service.entity.ChatMessage;
import com.quokka.Chat_Service.entity.ParticipantInfo;
import com.quokka.Chat_Service.exception.AppException;
import com.quokka.Chat_Service.exception.ErrorCode;
import com.quokka.Chat_Service.mapper.ChatMessageMapper;
import com.quokka.Chat_Service.repository.ChatMessageRepository;
import com.quokka.Chat_Service.repository.ConversationRepository;
import com.quokka.Chat_Service.repository.httpClient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatMessageService {

        ChatMessageRepository chatMessageRepository;
        ConversationRepository conversationRepository;
        ChatMessageMapper chatMessageMapper;
        ProfileClient profileClient;
        SocketIOServer socketIOServer;
        ObjectMapper objectMapper;

        public List<ChatMessageResponse> getMessages(String conversationId) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) auth.getPrincipal();
                String userId = jwt.getClaim("userId");

                var conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

                boolean isMember = conversation.getParticipants().stream()
                        .anyMatch(p -> p.getUserId().equals(userId));
                if (!isMember) {
                        throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
                }

                var messages = chatMessageRepository
                        .findAllByConversationIdOrderByCreatedDateAsc(conversationId);

                return messages.stream().map(this::toResponse).toList();
        }

        public ChatMessageResponse create(ChatMessageRequest request) throws JsonProcessingException {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) auth.getPrincipal();
                String userId = jwt.getClaim("userId");
                UUID uuidUserId = UUID.fromString(userId);

                // Check conversation
                var conversation = conversationRepository.findById(request.getConversationId())
                        .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

                boolean isMember = conversation.getParticipants().stream()
                        .anyMatch(p -> p.getUserId().equals(userId));
                if (!isMember) {
                        throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
                }

                // Get profile info
                var profileRes = profileClient.getProfile(uuidUserId);
                log.info("Profile response: {}", profileRes);
                if (Objects.isNull(profileRes) || profileRes.getData() == null)
                        throw new AppException(ErrorCode.UNAUTHORIZED);

                var user = profileRes.getData();
                log.info("Sending message from user: {}", user);

                // Build message
                ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
                chatMessage.setSender(ParticipantInfo.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .avatar(user.getAvatarUrl())
                        .build());
                chatMessage.setCreatedDate(Instant.now());
                chatMessage.setConversationId(request.getConversationId());

                chatMessage = chatMessageRepository.save(chatMessage);

                // Update last message in conversation
                conversation.setLastMessage(chatMessage.getMessage());
                conversation.setLastSenderId(userId);
                conversation.setLastMessageTime(Instant.now());
                conversation.setModifiedDate(Instant.now());
                conversationRepository.save(conversation);

                // Build response and broadcast
                ChatMessageResponse response = toResponse(chatMessage);
                String jsonResponse = objectMapper.writeValueAsString(response);

                // roadcast to the right room only!
                socketIOServer.getRoomOperations(request.getConversationId())
                        .sendEvent("message", jsonResponse);

                log.info("Message broadcasted to room {}: {}", request.getConversationId(), jsonResponse);

                return response;
        }

        private ChatMessageResponse toResponse(ChatMessage chat) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) auth.getPrincipal();
                String userId = jwt.getClaim("userId");
                var response = chatMessageMapper.toChatMessageResponse(chat);
                response.setMe(userId.equals(chat.getSender().getUserId()));
                return response;
        }
}
