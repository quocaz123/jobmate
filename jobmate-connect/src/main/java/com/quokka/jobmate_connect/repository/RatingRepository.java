package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.ApplicationStatus;
import com.quokka.jobmate_connect.entity.Rating;
import com.quokka.jobmate_connect.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // Rating mà một user nhận được
    Page<Rating> findByToUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Điểm trung bình của 1 user
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.toUser.id = :userId")
    Double getAverageRatingByUserId(@Param("userId") UUID userId);

    // Thống kê phân phối điểm của user
    @Query("SELECT r.score, COUNT(r) FROM Rating r WHERE r.toUser.id = :userId GROUP BY r.score ORDER BY r.score")
    List<Object[]> getRatingStatsByUserId(@Param("userId") UUID userId);

    Page<Rating> findByFromUserIdOrderByCreatedAtDesc(UUID fromUserId, Pageable pageable);

    boolean existsByFromUserIdAndToUserIdAndJobId(UUID fromUserId, UUID touserId, UUID jobId);

    long countByToUserId(UUID userId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.job.id = :jobId")
    Double getAverageRatingByJobId(@Param("jobId") UUID jobId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.job.id = :jobId")
    Long countByJobId(@Param("jobId") UUID jobId);
}
