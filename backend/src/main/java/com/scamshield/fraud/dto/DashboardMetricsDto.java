package com.scamshield.fraud.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDto {
    private int securityScore;
    private long totalThreatsBlocked;
    private long activeAlerts;
    private long scannedTransactions;
}
