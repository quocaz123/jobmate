package com.quokka.jobmate_connect.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ratings", uniqueConstraints =
        @UniqueConstraint(columnNames = {"from_user_id", "to_user_id", "job_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Người đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    User fromUser;

    // Người được đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    User toUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    Job job;

    @Column(nullable = false)
    Float score;

    @Column
    String comment;

    @Column
    LocalDateTime createdAt;

    @Column
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
