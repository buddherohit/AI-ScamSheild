package com.scamshield.fraud.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatIndicatorResponse {
    private Long id;
    private String type;
    private String value;
    private String source;
    private boolean isActive;
    private LocalDateTime createdAt;
}
