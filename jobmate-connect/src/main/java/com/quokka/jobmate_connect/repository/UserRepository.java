package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.entity.Role;
import com.quokka.jobmate_connect.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

        boolean existsByEmail(String email);

        Optional<User> findByEmail(String email);

        Page<User> findByVerificationStatus(VerificationStatus status, Pageable pageable);

        @Query("SELECT u.id from User u JOIN u.roles r WHERE r.name = 'ADMIN'")
        List<UUID> findAdminIds();

        Page<User> findByOrderByTrustScoreDesc(Pageable pageable);

        Page<User> findByBadgeLevelOrderByTrustScoreDesc(String badgeLevel, Pageable pageable);

        List<User> findTop10ByOrderByTrustScoreDesc();

        @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.roles r WHERE r.name = :roleName")
        long countByRoleName(@Param("roleName") String roleName);

        long countByCreatedAtGreaterThanEqual(LocalDateTime date);

        @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName ORDER BY COALESCE(u.violationCount, 0) DESC, u.createdAt DESC")
        Page<User> findTopByRoleOrderByViolationDesc(@Param("roleName") String roleName, Pageable pageable);

        @Query("SELECT DISTINCT u FROM User u " +
                        "LEFT JOIN u.roles r " +
                        "WHERE (:status IS NULL OR u.status = :status) " +
                        "AND (:role IS NULL OR r = :role) " +
                        "ORDER BY u.createdAt DESC")
        Page<User> findUserByStatus(@Param("status") String status,
                        Pageable pageable,
                        @Param("role") Role role);
}
