package com.quokka.jobmate_connect.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateRequest {
    @NotBlank(message = "Status không được để trống")
    @Pattern(regexp = "ACTIVE|BANNED", message = "Status phải là ACTIVE hoặc BANNED")
    String status;

    String reason;
}
