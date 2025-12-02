package com.quokka.jobmate_connect.dto.request.invatation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInvitationRequest {
    UUID candidateId;
    UUID waitingListId;
    UUID jobId;
    String message;
}
