package com.quokka.jobmate_connect.dto.request.job;


import com.quokka.jobmate_connect.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCreationRequest {
    String title;
    String description;
    String location;
    BigDecimal salary;
    String jobType;
    LocalDateTime startAt;
    LocalDateTime deadline;
    String skills;
    User createdBy;
}
