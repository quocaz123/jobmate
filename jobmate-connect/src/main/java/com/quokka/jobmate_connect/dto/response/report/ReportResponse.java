package com.quokka.jobmate_connect.dto.response.report;

import com.quokka.jobmate_connect.constant.ReportStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    UUID id;
    String targetType;
    UUID targetId;
    String reason;
    ReportStatus status;
    String reporterName;
    String reporterEmail;
    String adminNote;
    LocalDateTime createdAt;
    LocalDateTime reviewedAt;
}
