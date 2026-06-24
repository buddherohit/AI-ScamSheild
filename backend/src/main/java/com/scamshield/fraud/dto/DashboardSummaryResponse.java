package com.scamshield.fraud.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private DashboardMetricsDto metrics;
    private List<RecentActivityResponse> activity;
    private List<ThreatTrendResponse> threatTrends;
}
