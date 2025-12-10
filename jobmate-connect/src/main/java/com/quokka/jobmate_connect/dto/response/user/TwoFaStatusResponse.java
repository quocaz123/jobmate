package com.quokka.jobmate_connect.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFaStatusResponse {
    boolean enabled;
    String message;
}














