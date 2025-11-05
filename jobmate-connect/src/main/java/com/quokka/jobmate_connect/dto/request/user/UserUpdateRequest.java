package com.quokka.jobmate_connect.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    String fullName;
    String phoneNumber;
    String address;
    String avatarUrl;
    String skills;
    Double latitude;
    Double longitude;
}
