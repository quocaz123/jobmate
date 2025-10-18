package com.quokka.jobmate_connect.dto.request.otp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResendOtpRequest {
    String userId;
}
