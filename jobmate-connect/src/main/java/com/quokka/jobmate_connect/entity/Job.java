package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id")
    UUID id;

    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(length = 100)
    String location;

    @Column
    Double latitude;

    @Column
    Double longitude;

    @Column(precision = 12, scale = 2)
    BigDecimal salary;

    @Column(length = 100)
    String jobType;

    @Column(name = "start_at")
    LocalDateTime startAt;

    @Column(name = "deadline")
    LocalDateTime deadline;

    @Column(columnDefinition = "TEXT")
    String skills;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    JobStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User createdBy;

    @Column(name = "verified_by")
    UUID verifiedBy;

    @Column(name = "verified_at")
    LocalDateTime verifiedAt;

    @Column(name = "rejection_reason")
    String rejectionReason;

    @Column(name = "is_auto_verified")
    boolean isAutoVerified = false;

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt = LocalDateTime.now();
}
