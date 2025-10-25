package com.quokka.Chat_Service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantInfo {
    String userId;
    String email;
    String fullName;
    String avatar;
}
