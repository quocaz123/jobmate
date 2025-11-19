package com.quokka.jobmate_connect.entity;

import com.quokka.jobmate_connect.constant.JobType;
import com.quokka.jobmate_connect.constant.RequestStatus;
import com.quokka.jobmate_connect.constant.SalaryUnitType;
import com.quokka.jobmate_connect.constant.WaitingListStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "waiting_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaitingList {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Người tạo yêu cầu chờ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    // Loại công việc muốn tìm

    @Enumerated(EnumType.STRING)
    JobType jobType; // FULLTIME / PARTTIME

    // Kỹ năng mong muốn sử dụng
    @Column(columnDefinition = "TEXT")
    String skills;

    // Mức lương tối thiểu mong muốn
    @Column(precision = 12, scale = 2)
    BigDecimal expectedMinSalary;

    @Enumerated(EnumType.STRING)
    SalaryUnitType expectedSalaryUnit; // PER_HOUR / PER_DAY / PER_MONTH

    // Vị trí mong muốn làm việc
    @Column
    Double latitude;

    @Column
    Double longitude;

    // Bán kính tìm việc (km)
    @Column
    Integer searchRadius;

    // Lịch rảnh: ngày
    @Column
    String availableDays;

    // Lịch rảnh: buổi
    @Column
    String availableTime;

    // Ghi chú thêm
    @Column(columnDefinition = "TEXT")
    String note;

    @Enumerated(EnumType.STRING)
    RequestStatus status;

    @Column
    LocalDateTime createdAt;

    @Column
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        status = RequestStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


