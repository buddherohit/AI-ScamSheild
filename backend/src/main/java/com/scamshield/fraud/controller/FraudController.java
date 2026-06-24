package com.scamshield.fraud.controller;

import com.scamshield.common.ApiResponse;
import com.scamshield.fraud.dto.*;
import com.scamshield.fraud.entity.FraudCategory;
import com.scamshield.fraud.entity.ThreatIndicator;
import com.scamshield.fraud.repository.FraudCategoryRepository;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.RiskAssessmentRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.fraud.service.FraudReportService;
import com.scamshield.fraud.service.RiskAssessmentService;
import com.scamshield.fraud.service.ThreatIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud Intelligence", description = "Endpoints for fraud reporting, verification, and risk assessment")
public class FraudController {

    private final FraudReportService fraudReportService;
    private final ThreatIndicatorService threatIndicatorService;
    private final RiskAssessmentService riskAssessmentService;
    private final FraudCategoryRepository fraudCategoryRepository;
    private final FraudReportRepository fraudReportRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

    @PostMapping("/report")
    @Operation(summary = "Submit a new fraud report")
    public ResponseEntity<ApiResponse<FraudReportResponse>> submitReport(@Valid @RequestBody FraudReportRequest request) {
        FraudReportResponse response = fraudReportService.createReport(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Fraud report submitted successfully"));
    }

    @GetMapping("/report/{id}")
    @Operation(summary = "Get fraud report details by ID")
    public ResponseEntity<ApiResponse<FraudReportResponse>> getReport(@PathVariable Long id) {
        FraudReportResponse response = fraudReportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get all fraud reports with search and pagination")
    public ResponseEntity<ApiResponse<Page<FraudReportResponse>>> getReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FraudReportResponse> response = fraudReportService.searchReports(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify an entity and calculate its risk score")
    public ResponseEntity<ApiResponse<FraudVerifyResponse>> verifyEntity(@Valid @RequestBody FraudVerifyRequest request) {
        FraudVerifyResponse response = riskAssessmentService.assessRisk(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Verification complete"));
    }

    @PostMapping("/assess-risk")
    @Operation(summary = "Assess risk score for an entity and record snapshot")
    public ResponseEntity<ApiResponse<FraudVerifyResponse>> assessRisk(@Valid @RequestBody FraudVerifyRequest request) {
        FraudVerifyResponse response = riskAssessmentService.assessRisk(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Risk assessment complete"));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all fraud categories")
    public ResponseEntity<ApiResponse<List<FraudCategory>>> getCategories() {
        List<FraudCategory> categories = fraudCategoryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get system overview statistics")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getStatistics() {
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

    // ==============================================================================
    // Administrative Endpoints (RBAC secured)
    // ==============================================================================

    @PostMapping("/report/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Approve a fraud report")
    public ResponseEntity<ApiResponse<FraudReportResponse>> approveReport(@PathVariable Long id) {
        FraudReportResponse response = fraudReportService.approveReport(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Report approved"));
    }

    @PostMapping("/report/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Reject a fraud report")
    public ResponseEntity<ApiResponse<FraudReportResponse>> rejectReport(@PathVariable Long id) {
        FraudReportResponse response = fraudReportService.rejectReport(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Report rejected"));
    }

    @PostMapping("/report/{id}/flag")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Flag a fraud report")
    public ResponseEntity<ApiResponse<FraudReportResponse>> flagReport(@PathVariable Long id) {
        FraudReportResponse response = fraudReportService.flagReport(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Report flagged"));
    }

    @PostMapping("/indicator")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Add a new threat indicator")
    public ResponseEntity<ApiResponse<ThreatIndicatorResponse>> addIndicator(@Valid @RequestBody ThreatIndicatorRequest request) {
        ThreatIndicatorResponse response = threatIndicatorService.addIndicator(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Threat indicator added successfully"));
    }

    @PutMapping("/indicator/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Update a threat indicator")
    public ResponseEntity<ApiResponse<ThreatIndicatorResponse>> updateIndicator(@PathVariable Long id, @Valid @RequestBody ThreatIndicatorRequest request) {
        ThreatIndicatorResponse response = threatIndicatorService.updateIndicator(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Threat indicator updated successfully"));
    }

    @PostMapping("/indicator/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Deactivate a threat indicator")
    public ResponseEntity<ApiResponse<ThreatIndicatorResponse>> deactivateIndicator(@PathVariable Long id) {
        ThreatIndicatorResponse response = threatIndicatorService.deactivateIndicator(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Threat indicator deactivated"));
    }

    @GetMapping("/indicators")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Search threat indicators")
    public ResponseEntity<ApiResponse<Page<ThreatIndicatorResponse>>> getIndicators(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ThreatIndicatorResponse> response = threatIndicatorService.searchIndicators(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
