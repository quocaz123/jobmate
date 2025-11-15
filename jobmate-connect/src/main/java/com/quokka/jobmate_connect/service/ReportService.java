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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

    // ------------------------------------------
    // USER gửi report
    // ------------------------------------------
    @Transactional
    public ReportResponse createReport(ReportRequest request) {
        User reporter = getCurrentUser();

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

        autoReviewReport(report);

        if ("JOB".equalsIgnoreCase(request.getTargetType())) {
            autoHandleJobReport(report);
        }

        return reportMapper.toReportResponse(report);
    }

    // ------------------------------------------
    // ADMIN xem danh sách report
    // ------------------------------------------
    public PageResponse<ReportResponse> getReports(ReportStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Report> reports = (status != null)
                ? reportRepository.findByStatus(status, pageable)
                : reportRepository.findAll(pageable);

        return PageResponse.<ReportResponse>builder()
                .currentPage(reports.getNumber())
                .pageSize(size)
                .totalElements(reports.getTotalElements())
                .totalPages(reports.getTotalPages())
                .data(reports.map(reportMapper::toReportResponse).getContent())
                .build();
    }

    // ------------------------------------------
    // ADMIN duyệt / từ chối report
    // ------------------------------------------
    @Transactional
    public void reviewReport(UUID reportId, boolean accept, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        report.setStatus(accept ? ReportStatus.REVIEWED : ReportStatus.REJECTED);
        report.setAdminNote(adminNote);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        if (accept && "JOB".equalsIgnoreCase(report.getTargetType())) {
            autoHandleJobReport(report);
        }

        log.info("🧾 Admin reviewed report [{}] => {}", reportId, report.getStatus());
    }

    // ------------------------------------------
    // Auto review report
    // ------------------------------------------
    private void autoReviewReport(Report report) {
        String reason = report.getReason() != null ? report.getReason().toLowerCase() : "";

        // Kiểm tra từ khóa xấu từ file config
        boolean containsBadWord = reportProperties.getBadKeywords().stream()
                .anyMatch(reason::contains);

        long reviewedCount = reportRepository.countByTargetIdAndStatus(report.getTargetId(), ReportStatus.REVIEWED);

        if (containsBadWord || reviewedCount >= 2) {
            report.setStatus(ReportStatus.REVIEWED);
            report.setReviewedAt(LocalDateTime.now());
            reportRepository.save(report);
            log.info("Auto-reviewed report [{}] marked as REVIEWED", report.getId());
        }
    }

    // ------------------------------------------
    // Auto handle job report (auto close)
    // ------------------------------------------
    private void autoHandleJobReport(Report report) {
        UUID jobId = report.getTargetId();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        LocalDateTime window = LocalDateTime.now().minusDays(reportProperties.getJob().getWindowDays());
        long validReports = reportRepository.findByTargetId(jobId).stream()
                .filter(r -> r.getStatus() == ReportStatus.REVIEWED)
                .filter(r -> r.getCreatedAt().isAfter(window))
                .count();

        if (validReports >= reportProperties.getJob().getThreshold() && job.getStatus() != JobStatus.CLOSED) {
            job.setStatus(JobStatus.CLOSED);
            jobRepository.save(job);

            User employer = job.getCreatedBy();
            employer.setViolationCount((employer.getViolationCount() != null ? employer.getViolationCount() : 0) + 1);
            userRepository.save(employer);

            // kiểm tra có cần khóa account không
            autoLockEmployerIfExceedLimit(employer);

            notificationService.sendNotification(NotificationRequest.builder()
                    .userId(employer.getId())
                    .title("🚫 Job của bạn đã bị đóng tự động")
                    .message("Công việc '" + job.getTitle() + "' đã bị hệ thống đóng do nhiều báo cáo hợp lệ.")
                    .build());
        }
    }

    // ------------------------------------------
    // Cron job hằng ngày
    // ------------------------------------------
    @Scheduled(cron = "0 0 2 * * *") // chạy mỗi ngày 2h sáng
    @Transactional
    public void dailyReportScan() {
        log.info("🧹 Daily report scan running...");
        List<Job> jobs = jobRepository.findAll();

        for (Job job : jobs) {
            long validReports = reportRepository.countByTargetIdAndStatus(job.getId(), ReportStatus.REVIEWED);
            if (validReports >= reportProperties.getJob().getThreshold() && job.getStatus() != JobStatus.AUTO_CLOSED) {
                job.setStatus(JobStatus.CLOSED);
                jobRepository.save(job);

                User employer = job.getCreatedBy();
                employer.setViolationCount(
                        (employer.getViolationCount() != null ? employer.getViolationCount() : 0) + 1);
                userRepository.save(employer);

                autoLockEmployerIfExceedLimit(employer);
            }
        }
    }

    // ------------------------------------------
    // Tự động khóa employer nếu vượt ngưỡng vi phạm
    // ------------------------------------------
    private void autoLockEmployerIfExceedLimit(User employer) {
        int violationCount = employer.getViolationCount() != null ? employer.getViolationCount() : 0;
        if (violationCount >= reportProperties.getEmployer().getViolationLimit()
                && !"BANNED".equalsIgnoreCase(employer.getStatus())) {

            employer.setStatus("BANNED");
            userRepository.save(employer);

            notificationService.sendNotification(NotificationRequest.builder()
                    .userId(employer.getId())
                    .title("Tài khoản của bạn đã bị khóa")
                    .message("Tài khoản đã bị khóa do có quá nhiều vi phạm ("
                            + violationCount
                            + " lần). Vui lòng liên hệ quản trị viên để được hỗ trợ.")
                    .build());

            log.warn("Employer [{}] locked automatically after {} violations",
                    employer.getEmail(), violationCount);
        }
    }

    // ------------------------------------------
    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}
