package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reporter_id", "target_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    User reporter;

    @Column(nullable = false)
    String targetType;

    @Column(nullable = false)
    UUID targetId;

    @Column(length = 500)
    String reason;

    @Enumerated(EnumType.STRING)
    ReportStatus status;

    @Column
    String adminNote;

    @Column
    LocalDateTime createdAt;

    @Column
    LocalDateTime reviewedAt;

    @Column
    UUID reviewedBy;

    @Column
    Boolean canAppeal = true;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
