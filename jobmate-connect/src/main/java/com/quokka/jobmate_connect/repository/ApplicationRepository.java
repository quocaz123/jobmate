package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.entity.Application;
import com.quokka.jobmate_connect.constant.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    // Tìm application theo job và user
    Optional<Application> findByJobIdAndUserId(UUID jobId, UUID userId);

    // Lấy tất cả applications của một user
    Page<Application> findByUserIdOrderByAppliedAtDesc(UUID userId, Pageable pageable);

    // Lấy tất cả applications của một job
    Page<Application> findByJobIdOrderByAppliedAtDesc(UUID jobId, Pageable pageable);

    // Đếm số applications theo status
    long countByJobIdAndStatus(UUID jobId, ApplicationStatus status);

    // Kiểm tra user đã apply job chưa
    boolean existsByJobIdAndUserId(UUID jobId, UUID userId);

    // Lấy applications theo status
    Page<Application> findByStatusOrderByAppliedAtDesc(ApplicationStatus status, Pageable pageable);

    // Lấy applications của employer (qua job)
    @Query("SELECT a FROM Application a WHERE a.job.createdBy.id = :employerId ORDER BY a.appliedAt DESC")
    Page<Application> findByEmployerId(@Param("employerId") UUID employerId, Pageable pageable);

    // Thống kê applications theo job
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.id = :jobId GROUP BY a.status")
    List<Object[]> getApplicationStatsByJob(@Param("jobId") UUID jobId);
}
