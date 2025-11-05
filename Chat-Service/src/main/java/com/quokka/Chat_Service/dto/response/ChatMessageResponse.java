package com.quokka.Chat_Service.dto.response;

import com.quokka.Chat_Service.entity.ParticipantInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String id;
    String conversationId;
    boolean me;
    String message;
    ParticipantInfo sender;
    String createdDate;
}
