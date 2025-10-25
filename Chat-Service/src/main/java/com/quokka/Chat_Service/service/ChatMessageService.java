package com.quokka.Chat_Service.service;

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
                                .findAllByConversationIdOrderByCreatedDateDesc(conversationId);

                return messages.stream()
                                .map(this::toResponse)
                                .toList();
        }

        public ChatMessageResponse create(ChatMessageRequest request) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Jwt jwt = (Jwt) auth.getPrincipal();
                String userId = jwt.getClaim("userId");

                UUID uuidUserId = UUID.fromString(userId);

                var conversation = conversationRepository.findById(request.getConversationId())
                                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

                boolean isMember = conversation.getParticipants().stream()
                                .anyMatch(p -> p.getUserId().equals(userId));
                if (!isMember) {
                        throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
                }

                var profileRes = profileClient.getProfile(uuidUserId);
                if (Objects.isNull(profileRes) || profileRes.getData() == null)
                        throw new AppException(ErrorCode.UNAUTHORIZED);

                var user = profileRes.getData();

                log.info("User profile data: {}", user);
                log.info("User avatarUrl: {}", user.getAvatarUrl());

                ChatMessage message = chatMessageMapper.toChatMessage(request);

                message.setSender(ParticipantInfo.builder()
                                .userId(user.getId().toString())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl()
                                                : "https://via.placeholder.com/150")
                                .build());

                message.setCreatedDate(Instant.now());
                message.setConversationId(request.getConversationId());

                message = chatMessageRepository.save(message);

                log.info("Saved message createdDate: {}", message.getCreatedDate());

                return toResponse(message);
        }

        private ChatMessageResponse toResponse(ChatMessage chat) {
                String userId = SecurityContextHolder.getContext().getAuthentication().getName();
                var response = chatMessageMapper.toChatMessageResponse(chat);
                response.setMe(userId.equals(chat.getSender().getUserId()));
                return response;
        }
}
