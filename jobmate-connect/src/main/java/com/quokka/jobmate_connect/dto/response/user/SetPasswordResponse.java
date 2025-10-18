package com.quokka.jobmate_connect.dto.response.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetPasswordResponse {
    String message;
    boolean success;
    String redirectUrl; // URL để redirect sau khi set password thành công
}
