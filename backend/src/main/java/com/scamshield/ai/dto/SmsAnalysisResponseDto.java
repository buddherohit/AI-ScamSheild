package com.scamshield.ai.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAnalysisResponseDto {
    private Long id;
    private String smsText;
    private int riskScore;
    private String riskLevel;
    private String category;
    private String summary;
    private String recommendation;
    private List<String> indicators;
    private String simpleExplanation;
    private String technicalExplanation;
    private LocalDateTime createdAt;
}
