package com.quokka.Notification_Service.entity;

import com.quokka.Notification_Service.constant.NotificationType;
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

    @Column(nullable = false)
    UUID userId;

    @Column
    String title;

    @Column
    String message;

    @Enumerated(EnumType.STRING)
    NotificationType type;

    @Column
    boolean isRead;

    @Column
    LocalDateTime createdAt = LocalDateTime.now();



}
