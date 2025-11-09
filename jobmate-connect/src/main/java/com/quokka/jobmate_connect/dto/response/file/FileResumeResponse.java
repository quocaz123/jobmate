package com.quokka.jobmate_connect.dto.response.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResumeResponse {
    UUID id;
    String type;
    @JsonIgnore
    String url;
    String fileName;
    String contentType;
    long size;
    LocalDateTime createdAt;
}
