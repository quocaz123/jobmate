package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Employer gửi lời mời
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    User employer;

    // Candidate nhận lời mời
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    User candidate;

    // Job tuyển dụng (có hoặc không)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    Job job;

    // WaitingList ứng viên tạo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    WaitingList waitingList;

    @Enumerated(EnumType.STRING)
    InvitationStatus status;

    @Column(columnDefinition = "TEXT")
    String message;

    @Column
    LocalDateTime createdAt;

    @Column
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        status = InvitationStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
