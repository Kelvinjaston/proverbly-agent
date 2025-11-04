package com.example.proverb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "proverb")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proverb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    @Column(length = 1000)
    private String proverb;

    @Column(length = 1000)
    private String translation;

    @Column(length = 1000)
    private String meaning;

    @Column(length = 500)
    private String author;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String text;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
