package com.quokka.jobmate_connect.dto.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationRequest {

    @NotNull
    UUID jobId;

    String coverLetter;

    MultipartFile resumeFile;

    boolean useProfileResume;
}
