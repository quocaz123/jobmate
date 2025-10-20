package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String password;

    @Column
    String fullName;

    @Column
    String phoneNumber;

    @Column
    String address;

    @Column
    double latitude;

    @Column
    double longitude;

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

    @Column
    LocalDateTime createdAt;

    @Column
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}