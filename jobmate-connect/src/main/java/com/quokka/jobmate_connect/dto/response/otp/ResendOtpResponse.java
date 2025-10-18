package com.quokka.jobmate_connect.dto.response.otp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResendOtpResponse {
    String message;
    Long otpExpiryTime;
}
