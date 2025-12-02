package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.ReportStatus;
import com.quokka.jobmate_connect.dto.response.dashboard.AdminDashboardSummaryResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.AdminSystemHealthResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.AdminViolationUserResponse;
import com.quokka.jobmate_connect.dto.response.dashboard.ServiceHealthDetailResponse;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.repository.JobRepository;
import com.quokka.jobmate_connect.repository.ReportRepository;
import com.quokka.jobmate_connect.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminDashboardService {

    static String EMPLOYER_ROLE = "EMPLOYER";

    UserRepository userRepository;
    JobRepository jobRepository;
    ReportRepository reportRepository;
    StringRedisTemplate stringRedisTemplate;
    ObjectProvider<KafkaTemplate<?, ?>> kafkaTemplates;
    S3Client s3Client;

    @NonFinal
    @Value("${aws.s3.bucket-name:}")
    String bucketName;

    public AdminDashboardSummaryResponse getSummary() {
        long totalUsers = userRepository.count();
        long totalEmployers = userRepository.countByRoleName(EMPLOYER_ROLE);
        long totalJobs = jobRepository.count();
        long totalReports = reportRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();

        long newUsersToday = userRepository.countByCreatedAtGreaterThanEqual(startOfToday);
        long newUsersWeek = userRepository.countByCreatedAtGreaterThanEqual(startOfWeek);

        long pendingJobs = jobRepository.countByStatus(JobStatus.PENDING_REVIEW);
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);

        return AdminDashboardSummaryResponse.builder()
                .totalUsers(safeInt(totalUsers))
                .totalEmployers(safeInt(totalEmployers))
                .totalJobs(safeInt(totalJobs))
                .totalReports(safeInt(totalReports))
                .newUsersToday(safeInt(newUsersToday))
                .newUsersThisWeek(safeInt(newUsersWeek))
                .pendingJobs(safeInt(pendingJobs))
                .pendingReports(safeInt(pendingReports))
                .build();
    }

    public AdminSystemHealthResponse getSystemHealth() {
        List<ServiceHealthDetailResponse> details = new ArrayList<>();
        details.add(checkDatabase());
        details.add(checkRedis());
        details.add(checkKafka());
        details.add(checkS3());

        String overall = "UP";
        boolean hasUnknown = false;
        for (ServiceHealthDetailResponse detail : details) {
            if ("DOWN".equals(detail.getStatus())) {
                overall = "DOWN";
                break;
            }
            if ("UNKNOWN".equals(detail.getStatus())) {
                hasUnknown = true;
            }
        }
        if (!"DOWN".equals(overall) && hasUnknown) {
            overall = "DEGRADED";
        }

        return AdminSystemHealthResponse.builder()
                .overallStatus(overall)
                .services(details)
                .build();
    }

    public List<AdminViolationUserResponse> getTopViolationEmployers(int limit) {
        int sanitized = sanitizeLimit(limit, 5, 50);
        Page<User> page = userRepository.findTopByRoleOrderByViolationDesc(
                EMPLOYER_ROLE, PageRequest.of(0, sanitized));

        return page.getContent().stream()
                .map(user -> AdminViolationUserResponse.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .violationCount(user.getViolationCount())
                        .status(user.getStatus())
                        .build())
                .toList();
    }

    private ServiceHealthDetailResponse checkDatabase() {
        try {
            userRepository.count();
            return ServiceHealthDetailResponse.builder()
                    .name("database")
                    .status("UP")
                    .message("Kết nối cơ sở dữ liệu ổn định")
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (Exception ex) {
            log.error("Database health check failed", ex);
            return ServiceHealthDetailResponse.builder()
                    .name("database")
                    .status("DOWN")
                    .message(ex.getMessage())
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
    }

    private ServiceHealthDetailResponse checkRedis() {
        try {
            var connectionFactory = stringRedisTemplate.getRequiredConnectionFactory();
            var connection = connectionFactory.getConnection();
            try {
                connection.ping();
            } finally {
                connection.close();
            }
            return ServiceHealthDetailResponse.builder()
                    .name("redis")
                    .status("UP")
                    .message("Redis ping OK")
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (Exception ex) {
            log.warn("Redis health check failed", ex);
            return ServiceHealthDetailResponse.builder()
                    .name("redis")
                    .status("DOWN")
                    .message(ex.getMessage())
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
    }

    private ServiceHealthDetailResponse checkKafka() {
        KafkaTemplate<?, ?> kafkaTemplate = kafkaTemplates.stream().findFirst().orElse(null);
        if (kafkaTemplate == null) {
            return ServiceHealthDetailResponse.builder()
                    .name("kafka")
                    .status("UNKNOWN")
                    .message("KafkaTemplate không khả dụng")
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
        try {
            kafkaTemplate.metrics();
            return ServiceHealthDetailResponse.builder()
                    .name("kafka")
                    .status("UP")
                    .message("Kafka producer metrics OK")
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (Exception ex) {
            log.warn("Kafka health check failed", ex);
            return ServiceHealthDetailResponse.builder()
                    .name("kafka")
                    .status("DOWN")
                    .message(ex.getMessage())
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
    }

    private ServiceHealthDetailResponse checkS3() {
        if (bucketName == null || bucketName.isBlank()) {
            return ServiceHealthDetailResponse.builder()
                    .name("s3")
                    .status("UNKNOWN")
                    .message("Chưa cấu hình aws.s3.bucket-name")
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return ServiceHealthDetailResponse.builder()
                    .name("s3")
                    .status("UP")
                    .message("S3 headBucket OK")
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (S3Exception ex) {
            log.warn("S3 health check failed: {}", ex.awsErrorDetails().errorMessage());
            return ServiceHealthDetailResponse.builder()
                    .name("s3")
                    .status("DOWN")
                    .message(ex.awsErrorDetails().errorMessage())
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (Exception ex) {
            log.warn("S3 health check failed", ex);
            return ServiceHealthDetailResponse.builder()
                    .name("s3")
                    .status("DOWN")
                    .message(ex.getMessage())
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
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

