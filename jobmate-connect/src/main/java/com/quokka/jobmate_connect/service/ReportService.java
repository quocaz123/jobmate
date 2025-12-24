package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.configuration.ReportProperties;
import com.quokka.jobmate_connect.constant.*;
import com.quokka.jobmate_connect.dto.PageResponse;
import com.quokka.jobmate_connect.dto.request.notification.NotificationRequest;
import com.quokka.jobmate_connect.dto.request.report.ReportRequest;
import com.quokka.jobmate_connect.dto.response.report.ReportResponse;
import com.quokka.jobmate_connect.entity.*;
import com.quokka.jobmate_connect.exception.*;
import com.quokka.jobmate_connect.mapper.ReportMapper;
import com.quokka.jobmate_connect.repository.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReportService {

    ReportRepository reportRepository;
    UserRepository userRepository;
    JobRepository jobRepository;
    ReportMapper reportMapper;
    NotificationService notificationService;
    ReportProperties reportProperties;
    AuditLogService auditLogService;

    @Transactional
    public ReportResponse createReport(ReportRequest request) {

        User reporter = getCurrentUser();

        // Chặn account mới tạo < 24h
        if (reporter.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24))) {
            throw new AppException(ErrorCode.REPORTER_TOO_NEW);
        }

        // Chặn gửi report trùng
        if (reportRepository.existsByReporter_IdAndTargetId(reporter.getId(), request.getTargetId())) {
            throw new AppException(ErrorCode.REPORT_ALREADY_SUBMITTED);
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);

        auditLogService.record(reporter, AuditAction.REPORT_CREATE, report.getId(),
                report.getTargetType() + ":" + report.getTargetId(),
                "Lý do: " + request.getReason());

        autoReviewReport(report);

        if ("JOB".equalsIgnoreCase(request.getTargetType()) &&
                report.getStatus() == ReportStatus.REVIEWED) {
            autoHandleJobReport(report);
        }

        return mapReportsWithDetails(List.of(report)).get(0);
    }

    public PageResponse<ReportResponse> getReports(ReportStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Report> reports = (status != null)
                ? reportRepository.findByStatus(status, pageable)
                : reportRepository.findAll(pageable);

        List<ReportResponse> data = mapReportsWithDetails(reports.getContent());

        return PageResponse.<ReportResponse>builder()
                .currentPage(reports.getNumber())
                .pageSize(size)
                .totalElements(reports.getTotalElements())
                .totalPages(reports.getTotalPages())
                .data(data)
                .build();
    }

    @Transactional
    public void reviewReport(UUID reportId, boolean accept, String adminNote) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        User reviewer = getCurrentUser();
        UUID reviewerId = reviewer.getId();
        report.setReviewedBy(reviewerId);
        report.setReviewedAt(LocalDateTime.now());
        report.setStatus(accept ? ReportStatus.REVIEWED : ReportStatus.REJECTED);
        report.setAdminNote(adminNote);
        reportRepository.save(report);

        auditLogService.record(reviewer,
                accept ? AuditAction.REPORT_REVIEW_APPROVE : AuditAction.REPORT_REVIEW_REJECT,
                report.getId(),
                report.getTargetType() + ":" + report.getTargetId(),
                adminNote);

        if (accept && "JOB".equalsIgnoreCase(report.getTargetType())) {
            autoHandleJobReport(report);
        }

    }

    private void autoReviewReport(Report report) {

        String reason = Optional.ofNullable(report.getReason()).orElse("").toLowerCase();

        // match theo mức độ: critical / medium / low
        boolean isCritical = matchKeywordGroup(reason, "critical");
        boolean isMedium = matchKeywordGroup(reason, "medium");

        long reviewedCount = reportRepository.countByTargetIdAndStatus(report.getTargetId(), ReportStatus.REVIEWED);

        // auto REVIEWED logic
        if (isCritical || (isMedium && reviewedCount >= 1) || reviewedCount >= 2) {
            report.setStatus(ReportStatus.REVIEWED);
            report.setReviewedAt(LocalDateTime.now());
            report.setReviewedBy(null); // System reviewed
            reportRepository.save(report);

            auditLogService.record((User) null, AuditAction.REPORT_AUTO_REVIEW, report.getId(),
                    report.getTargetType() + ":" + report.getTargetId(),
                    "Auto review bởi hệ thống");
        }
    }
    private boolean matchKeywordGroup(String text, String groupName) {
        List<String> list = reportProperties.getBadKeywords().get(groupName);
        if (list == null)
            return false;
        return list.stream()
                .anyMatch(k -> text.matches(".*\\b" + Pattern.quote(k.toLowerCase()) + "\\b.*"));
    }

    private void autoHandleJobReport(Report report) {

        UUID jobId = report.getTargetId();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // Không đóng nhiều lần
        if (job.getStatus() == JobStatus.AUTO_CLOSED)
            return;

        LocalDateTime window = LocalDateTime.now()
                .minusDays(reportProperties.getJob().getWindowDays());

        List<Report> validList = reportRepository.findByTargetId(jobId).stream()
                .filter(r -> r.getStatus() == ReportStatus.REVIEWED)
                .filter(r -> r.getCreatedAt().isAfter(window))
                .toList();

        // Phải có >= 2 reporter khác nhau
        long distinctUsers = validList.stream()
                .map(r -> r.getReporter().getId())
                .distinct()
                .count();

        if (distinctUsers < 2)
            return;

        // Trọng số dựa vào trustScore
        int weightedScore = validList.stream()
                .mapToInt(r -> {
                    float trust = Optional.ofNullable(r.getReporter().getTrustScore()).orElse(0f);
                    if (trust >= 50)
                        return 3;
                    if (trust >= 20)
                        return 2;
                    return 1;
                }).sum();

        if (weightedScore >= reportProperties.getJob().getThreshold()) {

            job.setStatus(JobStatus.AUTO_CLOSED);
            jobRepository.save(job);

            User employer = job.getCreatedBy();
            employer.setViolationCount(
                    Optional.ofNullable(employer.getViolationCount()).orElse(0) + 1);

            userRepository.save(employer);

            autoLockEmployerIfExceedLimit(employer);

            notificationService.sendNotification(NotificationRequest.builder()
                    .userId(employer.getId())
                    .title("⛔ Job của bạn đã bị đóng tự động")
                    .message(
                            "Job '" + job.getTitle() + "' bị đóng vì có " + weightedScore +
                                    " điểm báo cáo hợp lệ từ " + distinctUsers + " người khác nhau.")
                    .build());
            auditLogService.record((User) null, AuditAction.JOB_STATUS_CHANGE, job.getId(),
                    job.getTitle(),
                    "Đóng vì vượt ngưỡng báo cáo (" + weightedScore + ")");
        }
    }

    private void autoLockEmployerIfExceedLimit(User employer) {

        int violationCount = Optional.ofNullable(employer.getViolationCount()).orElse(0);

        if (violationCount >= reportProperties.getEmployer().getViolationLimit()
                && !"BANNED".equalsIgnoreCase(employer.getStatus())) {

            employer.setStatus("BANNED");
            userRepository.save(employer);

            notificationService.sendNotification(NotificationRequest.builder()
                    .userId(employer.getId())
                    .title("Tài khoản đã bị khóa")
                    .message("Bạn đã vượt quá số vi phạm cho phép. (" + violationCount + ").")
                    .build());
            auditLogService.record((User) null, AuditAction.USER_STATUS_CHANGE, employer.getId(),
                    employer.getEmail(),
                    "Khóa tự động do " + violationCount + " vi phạm");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }

    private List<ReportResponse> mapReportsWithDetails(List<Report> reportList) {
        if (reportList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Job> jobMap = loadJobsForReports(reportList);

        return reportList.stream()
                .map(report -> enrichReportResponse(report, jobMap.get(report.getTargetId())))
                .toList();
    }

    private Map<UUID, Job> loadJobsForReports(List<Report> reportList) {
        List<UUID> jobIds = reportList.stream()
                .filter(r -> "JOB".equalsIgnoreCase(r.getTargetType()))
                .map(Report::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (jobIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return jobRepository.findByIdIn(jobIds).stream()
                .collect(Collectors.toMap(Job::getId, job -> job));
    }

    private ReportResponse enrichReportResponse(Report report, Job job) {
        ReportResponse response = reportMapper.toReportResponse(report);

        if (job != null) {
            response.setJobTitle(job.getTitle());
            User creator = job.getCreatedBy();
            if (creator != null) {
                response.setJobOwnerId(creator.getId());
                response.setJobOwnerEmail(creator.getEmail());
                response.setJobOwnerFullName(creator.getFullName());
            }
        }

        return response;
    }
}
