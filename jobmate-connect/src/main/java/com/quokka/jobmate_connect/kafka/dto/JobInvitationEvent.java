package com.quokka.jobmate_connect.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInvitationEvent {
    String eventType; // "SENT", "ACCEPTED", "REJECTED"

    UUID invitationId;
    UUID employerId;
    String employerEmail;
    String employerFullName;

    UUID candidateId;
    String candidateEmail;
    String candidateFullName;

    UUID jobId;
    String jobTitle;

    String message; // Lời nhắn từ employer (nếu có)

    LocalDateTime timestamp;
}
