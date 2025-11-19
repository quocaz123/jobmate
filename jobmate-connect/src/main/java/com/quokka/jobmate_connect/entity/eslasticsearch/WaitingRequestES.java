package com.quokka.jobmate_connect.entity.eslasticsearch;

import com.quokka.jobmate_connect.constant.RequestStatus;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(indexName = "waiting_requests_index")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaitingRequestES {
    @Id
    String id;

    String userId;

    String jobType;
    String skills;

    Double expectedMinSalary; // Sử dụng Double thay vì BigDecimal để tránh Java module system issues
    String expectedSalaryUnit;

    GeoPoint location;
    Integer searchRadius;

    String availableDays;
    String availableTime;

    String note;

    RequestStatus status;

    Long createdAt; // Lưu dưới dạng timestamp (epoch milliseconds)

    // Getter để convert Long sang LocalDateTime
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        if (createdAt == null) {
            return null;
        }
        return Instant.ofEpochMilli(createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    // Setter để convert LocalDateTime sang Long
    public void setCreatedAtFromLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            this.createdAt = null;
        } else {
            this.createdAt = dateTime.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }
    }
}
