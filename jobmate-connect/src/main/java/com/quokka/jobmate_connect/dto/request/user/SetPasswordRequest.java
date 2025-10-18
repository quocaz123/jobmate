package com.quokka.jobmate_connect.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetPasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    String password;

    @NotBlank(message = "Confirm password is required")
    String confirmPassword;
}
