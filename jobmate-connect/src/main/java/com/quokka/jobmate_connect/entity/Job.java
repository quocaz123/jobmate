package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.JobStatus;
import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.constant.SalaryUnitType;
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

    @Column(columnDefinition = "TEXT")
    String requirements;

    @Column(columnDefinition = "TEXT")
    String benefits;

    @Column(length = 100)
    String location;

    @Column
    Double latitude;

    @Column
    Double longitude;

    @Column(precision = 12, scale = 2)
    BigDecimal salary;

    @Enumerated(EnumType.STRING)
    JobType jobType; // FULLTIME / PARTTIME

    @Column()
    LocalDateTime deadline;

    @Column(columnDefinition = "TEXT")
    String skills;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    JobStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User createdBy;

    @Column()
    UUID verifiedBy;

    @Column()
    LocalDateTime verifiedAt;

    @Column()
    String rejectionReason;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();

    @Column(length = 255)
    String companyName;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    Integer applicationCount = 0;

    @Enumerated(EnumType.STRING)
    SalaryUnitType salaryUnit;

    @Column(length = 255)
    String workingHours;

    @Column(length = 100)
    String workingDays;

    @Column(length = 20)
    String workMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @Column(length = 255)
    String categoryName;

    @Column(columnDefinition = "INTEGER DEFAULT 1")
    @Builder.Default
    Integer targetApplicants = 1;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    Integer viewsCount = 0;

    @Column(length = 20)
    String contactPhone;
}
