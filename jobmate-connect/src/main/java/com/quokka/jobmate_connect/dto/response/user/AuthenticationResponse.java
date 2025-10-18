package com.quokka.jobmate_connect.dto.response.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    String token;
    boolean isTwoFaEnabled;
    String message;
    Long otpExpiryTime;
    String userId;
    boolean requiresPasswordSetup; // Cần set password
    String userEmail; // Email để hiển thị
    String userName; // Tên user để hiển thị
}
