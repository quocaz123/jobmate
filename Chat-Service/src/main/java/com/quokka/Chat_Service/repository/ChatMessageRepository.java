package com.quokka.Chat_Service.repository;

import com.quokka.Chat_Service.entity.ChatMessage;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findAllByConversationIdOrderByCreatedDateAsc(String conversationId);

    void deleteByConversationId(String conversationId);
}
