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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column
    BigDecimal salary;

    @Column(length = 100)
    String location;

    @Column(length = 100)
    String jobType;

    @Column
    LocalDateTime startAt;

    @Column
    LocalDateTime deadline;

    @Column(columnDefinition = "TEXT")
    String skills;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    JobStatus status = JobStatus.OPEN;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    LocalDateTime updatedAt = LocalDateTime.now();
}
