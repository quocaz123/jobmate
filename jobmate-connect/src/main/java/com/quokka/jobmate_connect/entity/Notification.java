package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column
    String title;

    @Column
    String message;

    @Enumerated(EnumType.STRING)
    NotificationType type;

    @Column
    Boolean read;

    @Column
    LocalDateTime createdAt;

}
