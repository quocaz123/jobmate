package com.quokka.Chat_Service.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_messages")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    @MongoId
    String id;

    @Indexed
    String conversationId;

    String message;

    ParticipantInfo sender;

    @Indexed
    Instant createdDate;
}
