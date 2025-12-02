package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.SalaryUnitType;
import com.quokka.jobmate_connect.constant.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    @Column
    String fullName;

    @Column
    String contactPhone;

    @Column
    String address;

    @Column
    Double latitude;

    @Column
    Double longitude;

    @Column
    String avatarUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<Role> roles;

    @Column(name = "is_two_fa_enabled", nullable = false, columnDefinition = "boolean default false")
    boolean is_two_fa_enabled = false;

    @Enumerated(EnumType.STRING)
    VerificationStatus verificationStatus;

    @Column
    String rejectionReason;

    @Column
    LocalDateTime verifiedAt;

    @Column
    LocalDateTime verificationRequestedAt;

    @Column(columnDefinition = "FLOAT DEFAULT 0")
    Float trustScore = 0f;

    @Column
    Integer reviewCount = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    Integer violationCount = 0;

    @Column
    String badgeLevel;

    @Column
    String status;

    @Column
    LocalDateTime createdAt;

    @Column
    LocalDateTime updatedAt;

    @Column
    String skills;

    @Column
    String preferredJobType;

    @Column
    String availableDays;

    @Column
    String availableTime;

    @Column(columnDefinition = "TEXT")
    String bio;

    @Column(precision = 12, scale = 2)
    BigDecimal preferredMinSalary;

    @Enumerated(EnumType.STRING)
    SalaryUnitType preferredSalaryUnit;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
            trustScore = 0f;
            reviewCount = 0;
            violationCount = 0;
            badgeLevel = "None";
            status = "ACTIVE";
            verificationStatus = VerificationStatus.UNVERIFIED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}