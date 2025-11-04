package com.example.proverb.dto;

import jakarta.validation.constraints.NotBlank;

public class ProverbRequest {
    @NotBlank
    private String language;
    @NotBlank
    private String proverb;
    @NotBlank
    private String meaning;
}
