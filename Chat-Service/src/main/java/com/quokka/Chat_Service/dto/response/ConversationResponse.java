package com.quokka.Chat_Service.dto.response;

import com.quokka.Chat_Service.entity.ParticipantInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    String id;
    String type;
    String participantsHash;
    String conversationName;
    String conversationAvatar;
    List<ParticipantInfo> participants;
    String lastMessage;
    String lastSenderId;
    Instant lastMessageTime;
    Instant createdDate;
    Instant modifiedDate;

}
