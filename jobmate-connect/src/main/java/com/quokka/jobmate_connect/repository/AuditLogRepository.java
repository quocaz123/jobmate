package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    // Tìm audit log theo user
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    // Tìm audit log theo action
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    // Tìm audit log theo target ID
    Page<AuditLog> findByTargetIdOrderByCreatedAtDesc(UUID targetId, Pageable pageable);
    
    // Tìm audit log theo user và action
    Page<AuditLog> findByUserIdAndActionOrderByCreatedAtDesc(UUID userId, String action, Pageable pageable);
    
    // Tìm audit log trong khoảng thời gian
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    // Tìm audit log theo user trong khoảng thời gian
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId " +
           "AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    // Đếm số lượng audit log theo action
    long countByAction(String action);
}


