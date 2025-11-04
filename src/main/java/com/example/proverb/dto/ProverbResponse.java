package com.example.proverb.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProverbResponse {
    private Long id;
    private String language;
    private String proverb;
    private String translation;
    private String meaning;
}

