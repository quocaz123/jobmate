package com.quokka.Chat_Service.service;

import com.quokka.Chat_Service.dto.request.ConversationRequest;
import com.quokka.Chat_Service.dto.response.ConversationResponse;
import com.quokka.Chat_Service.entity.Conversation;
import com.quokka.Chat_Service.entity.ParticipantInfo;
import com.quokka.Chat_Service.exception.AppException;
import com.quokka.Chat_Service.exception.ErrorCode;
import com.quokka.Chat_Service.mapper.ConversationMapper;
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
public class ConversationService {
    ConversationRepository conversationRepository;
    ProfileClient profileClient;
    ConversationMapper conversationMapper;

    public List<ConversationResponse> myConversations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        String userId = jwt.getClaim("userId");

        var conversations = conversationRepository.findAllByParticipantIdsContains(userId);

        return conversations.stream()
                .map(conversationMapper::toConversationResponse)
                .toList();
    }

    public ConversationResponse create(ConversationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        UUID currentUserId = UUID.fromString(jwt.getClaim("userId"));

        var currentUserProfile = profileClient.getProfile(currentUserId);
        var otherUserProfile = profileClient.getProfile(UUID.fromString(request.getParticipantIds().get(0)));

        if (Objects.isNull(currentUserProfile) || Objects.isNull(otherUserProfile)
                || currentUserProfile.getData() == null || otherUserProfile.getData() == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        var currentUser = currentUserProfile.getData();
        var otherUser = otherUserProfile.getData();

        log.info("Current user avatarUrl: {}", currentUser.getAvatarUrl());
        log.info("Other user avatarUrl: {}", otherUser.getAvatarUrl());

        String stringCurrentUserId = currentUserId.toString();

        List<String> ids = List.of(stringCurrentUserId, otherUser.getId().toString()).stream().sorted().toList();
        String hash = generateHash(ids);

        var conversation = conversationRepository.findByParticipantsHash(hash).orElseGet(() -> {
            List<ParticipantInfo> participantInfos = List.of(
                    ParticipantInfo.builder()
                            .userId(currentUser.getId().toString())
                            .fullName(currentUser.getFullName())
                            .avatar(currentUser.getAvatarUrl())
                            .build(),
                    ParticipantInfo.builder()
                            .userId(otherUser.getId().toString())
                            .fullName(otherUser.getFullName())
                            .avatar(otherUser.getAvatarUrl())
                            .build());
            Conversation newConversation = Conversation.builder()
                    .type(request.getType())
                    .participantsHash(hash)
                    .participants(participantInfos)
                    .createdDate(Instant.now())
                    .modifiedDate(Instant.now())
                    .build();

            var savedConversation = conversationRepository.save(newConversation);
            return savedConversation;
        });

        return toResponse(conversation);
    }

    public List<ConversationResponse> searchConversations(String keyword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        String userId = jwt.getClaim("userId");

        if (keyword == null || keyword.trim().isEmpty()) {
            return myConversations();
        }

        var conversations = conversationRepository.searchByParticipantName(userId, keyword);

        return conversations.stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateHash(List<String> ids) {
        return String.join("_", ids);
    }

    private ConversationResponse toResponse(Conversation conversation) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        var res = conversationMapper.toConversationResponse(conversation);

        conversation.getParticipants().stream()
                .filter(p -> !p.getUserId().equals(currentUserId))
                .findFirst()
                .ifPresent(p -> {
                    res.setConversationName(p.getFullName());
                    res.setConversationAvatar(p.getAvatar());
                });
        return res;
    }
}
