package com.quokka.jobmate_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DefaultCategoriesConfig {
    @JsonProperty("categories")
    private List<String> categories;
}
