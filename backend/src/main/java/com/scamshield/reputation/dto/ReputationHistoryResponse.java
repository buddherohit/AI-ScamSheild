package com.scamshield.reputation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationHistoryResponse {
    private Long id;
    private String verifiedEntity;
    private String entityType;
    private int riskScore;
    private String riskLevel;
    private String status;
    private LocalDateTime createdAt;
}
