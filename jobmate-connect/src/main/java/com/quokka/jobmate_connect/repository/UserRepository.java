package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import com.quokka.jobmate_connect.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Page<User> findByVerificationStatus(VerificationStatus status);
}
