package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.dto.response.dashboard.EmployerDashboardSummaryResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.EmployerRecentCandidateResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.EmployerTopJobResponse;
import com.quokka.jobmate_connect.entity.Application;
import com.quokka.jobmate_connect.entity.Job;
import com.quokka.jobmate_connect.repository.ApplicationRepository;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.service.maching.MatchingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmployerDashboardService {

    JobRepository jobRepository;
    ApplicationRepository applicationRepository;
    MatchingService matchingService;

    public EmployerDashboardSummaryResponse getSummary() {
        UUID employerId = getCurrentEmployerId();
        long totalJobs = jobRepository.countByCreatedById(employerId);
        long activeJobs = jobRepository.countByCreatedByIdAndStatus(employerId, JobStatus.APPROVED);
        long pendingReviewJobs = jobRepository.countByCreatedByIdAndStatus(employerId, JobStatus.PENDING_REVIEW);
        long closedJobs = jobRepository.countByCreatedByIdAndStatusIn(
                employerId,
                List.of(JobStatus.CLOSED, JobStatus.AUTO_CLOSED, JobStatus.REJECTED));

        long totalApplications = applicationRepository.countByJob_CreatedBy_Id(employerId);
        long pendingApplications = applicationRepository.countByJob_CreatedBy_IdAndStatus(
                employerId, ApplicationStatus.PENDING);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long applicationsToday = applicationRepository.countByJob_CreatedBy_IdAndAppliedAtGreaterThanEqual(
                employerId, startOfDay);

        return EmployerDashboardSummaryResponse.builder()
                .totalJobs(safeInt(totalJobs))
                .activeJobs(safeInt(activeJobs))
                .pendingReviewJobs(safeInt(pendingReviewJobs))
                .closedJobs(safeInt(closedJobs))
                .totalApplications(safeInt(totalApplications))
                .pendingApplications(safeInt(pendingApplications))
                .applicationsToday(safeInt(applicationsToday))
                .build();
    }

    public List<EmployerTopJobResponse> getTopJobs(int limit) {
        UUID employerId = getCurrentEmployerId();
        int sanitizedLimit = sanitizeLimit(limit, 5, 10);
        Pageable pageable = PageRequest.of(
                0,
                sanitizedLimit,
                Sort.by(Sort.Direction.DESC, "applicationCount"));

        Page<Job> jobPage = jobRepository.findByCreatedByIdAndStatusNot(
                employerId, JobStatus.DELETED, pageable);

        return jobPage.getContent().stream()
                .map(job -> {
                    LocalDateTime lastAppliedAt = applicationRepository
                            .findFirstByJob_IdOrderByAppliedAtDesc(job.getId())
                            .map(Application::getAppliedAt)
                            .orElse(null);
                    int totalApplications = job.getApplicationCount() != null ? job.getApplicationCount() : 0;
                    int target = job.getTargetApplicants() != null ? job.getTargetApplicants() : 0;
                    return EmployerTopJobResponse.builder()
                            .jobId(job.getId())
                            .title(job.getTitle())
                            .totalApplications(totalApplications)
                            .targetApplicants(target)
                            .viewsCount(job.getViewsCount() != null ? job.getViewsCount() : 0)
                            .status(job.getStatus())
                            .lastApplicationAt(lastAppliedAt)
                            .targetReached(target > 0 && totalApplications >= target)
                            .build();
                })
                .toList();
    }

    public List<EmployerRecentCandidateResponse> getRecentCandidates(int limit) {
        UUID employerId = getCurrentEmployerId();
        int sanitizedLimit = sanitizeLimit(limit, 5, 20);
        Pageable pageable = PageRequest.of(0, sanitizedLimit);

        Page<Application> applications = applicationRepository.findByJob_CreatedBy_IdOrderByAppliedAtDesc(
                employerId, pageable);

        return applications.getContent().stream()
                .map(application -> {
                    double matchScore = 0;
                    try {
                        matchScore = matchingService.calculateMatchScore(application.getUser(), application.getJob());
                    } catch (Exception e) {
                        log.warn("Không thể tính match score cho ứng viên {}", application.getUser().getEmail(), e);
                    }
                    return EmployerRecentCandidateResponse.builder()
                            .applicationId(application.getId())
                            .candidateId(application.getUser().getId())
                            .candidateName(application.getUser().getFullName())
                            .candidateEmail(application.getUser().getEmail())
                            .jobId(application.getJob().getId())
                            .jobTitle(application.getJob().getTitle())
                            .jobStatus(application.getJob().getStatus())
                            .status(application.getStatus())
                            .appliedAt(application.getAppliedAt())
                            .matchScore(matchScore)
                            .resumeAvailable(application.isHasResume())
                            .build();
                })
                .toList();
    }

    private UUID getCurrentEmployerId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }

    private int safeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private int sanitizeLimit(int requested, int defaultValue, int maxValue) {
        if (requested <= 0) {
            return defaultValue;
        }
        return Math.min(requested, maxValue);
    }
}
