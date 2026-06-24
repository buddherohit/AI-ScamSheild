package com.scamshield.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_patterns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_key", nullable = false, unique = true, length = 50)
    private String patternKey;

    @Column(name = "pattern_regex", nullable = false, length = 255)
    private String patternRegex;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private int weight;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
