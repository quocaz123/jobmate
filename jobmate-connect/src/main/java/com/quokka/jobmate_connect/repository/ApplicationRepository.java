package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByUserIdAndJobId(UUID userId, UUID jobId);

    // Lấy tất cả applications của 1 job, sắp theo thời gian nộp (mới nhất trước)
    @EntityGraph(attributePaths = { "job", "user" })
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId ORDER BY a.appliedAt DESC")
    Page<Application> findByJobIdOrderByAppliedAtDesc(@Param("jobId") UUID jobId, Pageable pageable);

    // Lấy tất cả applications của 1 user, sắp theo thời gian nộp (mới nhất trước)
    @EntityGraph(attributePaths = { "job", "user" })
    @Query("SELECT a FROM Application a WHERE a.user.id = :userId ORDER BY a.appliedAt DESC")
    Page<Application> findByUserIdOrderByAppliedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.status != 'CANCELLED'")
    Long countByJobId(@Param("jobId") UUID jobId);

    // Đếm số lượng applications đã được chấp nhận (ACCEPTED) cho một job
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.status = 'ACCEPTED'")
    Long countAcceptedByJobId(@Param("jobId") UUID jobId);

    // Lấy application với eager loading job và user
    @EntityGraph(attributePaths = { "job", "user", "job.createdBy" })
    Optional<Application> findById(UUID id);

    Page<Application> findByJobIdAndStatusOrderByAppliedAtDesc(
            UUID jobId,
            ApplicationStatus status,
            Pageable pageable);

    boolean existsByUserIdAndJobIdAndStatusIn(UUID userId, UUID jobId, List<ApplicationStatus> statuses);

    long countByJob_CreatedBy_Id(UUID employerId);

    long countByJob_CreatedBy_IdAndStatus(UUID employerId, ApplicationStatus status);

    long countByJob_CreatedBy_IdAndAppliedAtGreaterThanEqual(UUID employerId, java.time.LocalDateTime from);

    @EntityGraph(attributePaths = { "job", "job.createdBy", "user" })
    @Query("SELECT a FROM Application a WHERE a.job.createdBy.id = :employerId ORDER BY a.appliedAt DESC")
    Page<Application> findByJob_CreatedBy_IdOrderByAppliedAtDesc(@Param("employerId") UUID employerId, Pageable pageable);

    java.util.Optional<Application> findFirstByJob_IdOrderByAppliedAtDesc(UUID jobId);

    long countByUser_Id(UUID userId);

    long countByUser_IdAndStatus(UUID userId, ApplicationStatus status);

    long countByUser_IdAndStatusIn(UUID userId, List<ApplicationStatus> statuses);

    // Lấy thời gian application mới nhất cho nhiều jobs
    @Query("SELECT a.job.id, MAX(a.appliedAt) FROM Application a WHERE a.job.id IN :jobIds GROUP BY a.job.id")
    List<Object[]> findLatestAppliedAtByJobIds(@Param("jobIds") List<UUID> jobIds);

}
