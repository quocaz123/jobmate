package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_mgmt", uniqueConstraints = @UniqueConstraint(columnNames = {"ownerId", "type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileMgmt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    UUID ownerId;

    @Enumerated(EnumType.STRING)
    FileTypeStatus type;

    @Column
    String s3Key;

    @Column
    String url;

    @Column
    String contentType;

    @Column
    long size;

    @Column
    LocalDateTime createdAt;
}
