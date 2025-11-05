package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.entity.Role;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Page<User> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    @Query("SELECT u.id from User u JOIN u.roles r WHERE r.name = 'ADMIN'")
    List<UUID> findAdminIds();

    Page<User> findByOrderByTrustScoreDesc(Pageable pageable);

    Page<User> findByBadgeLevelOrderByTrustScoreDesc(String badgeLevel, Pageable pageable);

    List<User> findTop10ByOrderByTrustScoreDesc();

}
