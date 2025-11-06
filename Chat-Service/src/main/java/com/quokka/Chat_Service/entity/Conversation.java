package com.quokka.Chat_Service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "conversations")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {
    @MongoId
    String id;

    String type;

    @Indexed(unique = true) // Khong dc trung lap
    String participantsHash;

    List<ParticipantInfo> participants;

    @Indexed
    String conversationName;
    String conversationAvatar;

    String lastMessage;

    String lastSenderId;

    Instant lastMessageTime;

    Instant createdDate;
    Instant modifiedDate;
}
