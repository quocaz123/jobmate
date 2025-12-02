package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.AuditAction;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.response.audit.AuditLogResponse;
import com.quokka.jobmate_connect.dto.response.audit.AuditLogStatsResponse;
import com.quokka.jobmate_connect.entity.AuditLog;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.repository.AuditLogRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import com.quokka.jobmate_connect.util.AuditLogMessageFormatter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogService {

    AuditLogRepository auditLogRepository;
    UserRepository userRepository;

    public void record(UUID userId, AuditAction action, UUID targetId, String description) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        record(user, action, targetId, description);
    }

    public void record(User user, AuditAction action, UUID targetId, String description) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action != null ? action.name() : null)
                .targetId(targetId)
                .description(description)
                .build();
        auditLogRepository.save(log);
    }

    public void record(User user, AuditAction action, UUID targetId, String subject, String... details) {
        record(user, action, targetId, AuditLogMessageFormatter.format(action, subject, details));
    }

    public void record(UUID userId, AuditAction action, UUID targetId, String subject, String... details) {
        record(userId, action, targetId, AuditLogMessageFormatter.format(action, subject, details));
    }

    public PageResponse<AuditLogResponse> getAuditLogs(
            int page,
            int size,
            UUID userId,
            AuditAction action,
            UUID targetId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AuditLog> spec = (root, query, cb) -> cb.conjunction();

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action.name()));
        }
        if (targetId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("targetId"), targetId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        Page<AuditLog> auditLogPage = auditLogRepository.findAll(spec, pageable);

        return PageResponse.<AuditLogResponse>builder()
                .currentPage(auditLogPage.getNumber())
                .pageSize(auditLogPage.getSize())
                .totalElements(auditLogPage.getTotalElements())
                .totalPages(auditLogPage.getTotalPages())
                .data(auditLogPage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .build();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        User user = log.getUser();
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(user != null ? user.getId() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userFullName(user != null ? user.getFullName() : null)
                .action(log.getAction())
                .targetId(log.getTargetId())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public List<AuditLogStatsResponse> getActionStatistics(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        int effectiveLimit = limit <= 0 ? 10 : limit;
        return auditLogRepository.aggregateActionCounts(startDate, endDate).stream()
                .map(row -> {
                    String actionName = row[0] != null ? row[0].toString() : null;
                    long total = row[1] instanceof Number number ? number.longValue() : 0L;
                    AuditAction action = null;
                    if (actionName != null) {
                        try {
                            action = AuditAction.valueOf(actionName);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    String label = action != null
                            ? action.getLabel()
                            : (actionName != null ? actionName : "UNKNOWN");
                    return new AuditLogStatsResponse(actionName, label, total);
                })
                .limit(effectiveLimit)
                .toList();
    }
}
