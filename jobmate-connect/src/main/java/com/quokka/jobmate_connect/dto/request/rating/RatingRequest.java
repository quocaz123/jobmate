package com.quokka.jobmate_connect.dto.request.rating;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingRequest {
    UUID toUserId;
    UUID jobId;

    @NotNull(message = "Score must not be null")
    @DecimalMin(value = "1.0", message = "Score must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Score must be at most 5.0")
    Float score;

    String comment;
}
