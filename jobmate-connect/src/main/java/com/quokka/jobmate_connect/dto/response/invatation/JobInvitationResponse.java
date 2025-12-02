package com.quokka.jobmate_connect.dto.response.invatation;

import com.quokka.jobmate_connect.constant.InvitationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInvitationResponse {
    UUID id;
    UUID employerId;
    UUID candidateId;
    UUID jobId;
    String title;
    UUID waitingListId;
    String message;
    InvitationStatus status;
    LocalDateTime createdAt;
}
