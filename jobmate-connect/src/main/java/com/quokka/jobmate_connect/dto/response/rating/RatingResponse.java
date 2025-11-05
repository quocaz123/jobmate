package com.quokka.jobmate_connect.dto.response.rating;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    UUID id;
    UUID fromUserId;
    String fromUserName;
    UUID toUserId;
    String toUserName;
    UUID jobId;
    String jobTitle;
    Float score;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
