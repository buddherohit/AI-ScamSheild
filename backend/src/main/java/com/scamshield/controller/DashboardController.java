package com.scamshield.controller;

import com.scamshield.common.ApiResponse;
import com.scamshield.fraud.dto.DashboardSummaryResponse;
import com.scamshield.fraud.dto.DashboardMetricsDto;
import com.scamshield.fraud.dto.RecentActivityResponse;
import com.scamshield.fraud.entity.ThreatIndicator;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.RiskAssessmentRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for user dashboard metrics and summaries")
public class DashboardController {

    private final FraudReportRepository fraudReportRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard overview summary metrics and recent activity")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        long totalReports = fraudReportRepository.count();
        long highRiskCount = riskAssessmentRepository.countByRiskLevel("CRITICAL") + riskAssessmentRepository.countByRiskLevel("HIGH");
        long activeThreatIndicators = threatIndicatorRepository.count();

        DashboardMetricsDto metrics = DashboardMetricsDto.builder()
                .securityScore(98)
                .totalThreatsBlocked(activeThreatIndicators + totalReports)
                .activeAlerts(highRiskCount)
                .scannedTransactions(1234)
                .build();

        List<RecentActivityResponse> activity = fraudReportRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .getContent().stream()
                .map(r -> RecentActivityResponse.builder()
                        .id(r.getId().toString())
                        .timestamp("Recent")
                        .type("alert")
                        .severity(r.getSeverity().toLowerCase())
                        .description("Report: " + r.getDescription())
                        .status(r.getStatus().toLowerCase())
                        .build())
                .collect(Collectors.toList());

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .metrics(metrics)
                .activity(activity)
                .build();

        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
