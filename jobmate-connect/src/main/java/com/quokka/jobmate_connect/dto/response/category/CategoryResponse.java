package com.quokka.jobmate_connect.dto.response.category;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CategoryResponse {
    UUID id;
    String name;
}

