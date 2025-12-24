package com.quokka.jobmate_connect.dto.request.invatation;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInvitationRequest {
    @NotNull
    UUID candidateId;
    @NotNull
    UUID waitingListId;
    @NotNull
    UUID jobId;
    String message;
}
