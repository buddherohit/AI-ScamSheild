package com.scamshield.fraud.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudReportResponse {
    private Long id;
    private String entityType;
    private String entityValue;
    private String categoryCode;
    private String categoryDisplayName;
    private String description;
    private String severity;
    private String status;
    private String reporterEmail;
    private LocalDateTime createdAt;
}
