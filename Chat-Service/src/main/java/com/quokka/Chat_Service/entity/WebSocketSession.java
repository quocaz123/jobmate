package com.quokka.Chat_Service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "websocket_sessions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebSocketSession {
    @MongoId
    String id;

    String socketSessionId;

    String userId;

    Instant createdDate;
}
