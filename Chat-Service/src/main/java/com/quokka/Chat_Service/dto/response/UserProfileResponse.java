package com.quokka.Chat_Service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    UUID id; // Thay đổi từ userId thành id
    String email;
    String fullName;
    String avatarUrl; // Thay đổi từ avatar thành avatarUrl
}
