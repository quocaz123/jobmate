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

    UUID reporterId;
    String reporterEmail;
    String reporterFullName;

    String targetType;
    UUID targetId;

    String jobTitle;
    UUID jobOwnerId;
    String jobOwnerEmail;
    String jobOwnerFullName;

    String reason;

    ReportStatus status;

    String adminNote; // admin ghi lý do duyệt/từ chối
    Boolean canAppeal; // employer có quyền khiếu nại không

    UUID reviewedBy;
    String reviewedByEmail;

    LocalDateTime createdAt;
    LocalDateTime reviewedAt;
}
