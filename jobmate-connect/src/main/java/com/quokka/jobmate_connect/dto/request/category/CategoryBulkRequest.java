package com.quokka.jobmate_connect.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CategoryBulkRequest {
    @NotEmpty(message = "Category list must not be empty")
    private List<@NotBlank(message = "Category name must not be blank") String> names;
}
