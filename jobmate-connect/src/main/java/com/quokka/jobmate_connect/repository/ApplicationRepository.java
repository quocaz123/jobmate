package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByJobIdAndUserId(UUID jobId, UUID userId);

    // Lấy tất cả applications của 1 job, sắp theo thời gian nộp (mới nhất trước)
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId ORDER BY a.appliedAt DESC")
    Page<Application> findByJobIdOrderByAppliedAtDesc(@Param("jobId") UUID jobId, Pageable pageable);

    // Lấy tất cả applications của 1 user, sắp theo thời gian nộp (mới nhất trước)
    @Query("SELECT a FROM Application a WHERE a.user.id = :userId ORDER BY a.appliedAt DESC")
    Page<Application> findByUserIdOrderByAppliedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.status != 'CANCELLED'")
    Long countByJobId(@Param("jobId") UUID jobId);
}
