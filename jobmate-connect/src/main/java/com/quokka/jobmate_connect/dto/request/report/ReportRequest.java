package com.quokka.jobmate_connect.dto.request.report;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportRequest {
    String targetType; // JOB, USER, RATING
    UUID targetId;
    String reason;
}
