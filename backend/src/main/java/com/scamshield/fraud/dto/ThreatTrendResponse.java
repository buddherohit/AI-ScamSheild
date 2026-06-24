package com.scamshield.fraud.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatTrendResponse {
    private String name;
    private int threats;
}
