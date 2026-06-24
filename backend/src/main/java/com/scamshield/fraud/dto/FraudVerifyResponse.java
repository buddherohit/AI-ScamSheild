package com.scamshield.fraud.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudVerifyResponse {
    private int riskScore;
    private String riskLevel;
    private List<String> reasons;
}
