package com.quokka.jobmate_connect.dto.response.rating;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    UUID id;
    Float score;
    String comment;
    LocalDateTime createdAt;

    // Người đánh giá
    UUID fromUserId;
    String fromUserName;
    String fromUserAvatar;

    // Người được đánh giá
    UUID toUserId;
    String toUserName;

    // Công việc liên quan
    UUID jobId;
    String jobTitle;
}
