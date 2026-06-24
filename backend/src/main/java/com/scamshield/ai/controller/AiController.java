package com.scamshield.ai.controller;

import com.scamshield.common.ApiResponse;
import com.scamshield.ai.dto.SmsAnalysisRequestDto;
import com.scamshield.ai.dto.SmsAnalysisResponseDto;
import com.scamshield.ai.service.SmsAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI SMS Fraud Detection & Intelligence", description = "Endpoints for analyzing SMS text for scams, managing analysis logs and insights")
public class AiController {

    private final SmsAnalysisService smsAnalysisService;

    @PostMapping("/analyze-sms")
    @Operation(summary = "Analyze any SMS message for scams and return comprehensive threat profiles")
    public ResponseEntity<ApiResponse<SmsAnalysisResponseDto>> analyzeSms(
            @Valid @RequestBody SmsAnalysisRequestDto request,
            HttpServletRequest httpServletRequest) {
        
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }

        SmsAnalysisResponseDto response = smsAnalysisService.analyzeSms(request.getSmsText(), ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "SMS Analysis completed successfully"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated analysis history for the current user")
    public ResponseEntity<ApiResponse<Page<SmsAnalysisResponseDto>>> getHistory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SmsAnalysisResponseDto> response = smsAnalysisService.getHistory(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analysis/{id}")
    @Operation(summary = "Get detailed analysis report by ID")
    public ResponseEntity<ApiResponse<SmsAnalysisResponseDto>> getAnalysisDetails(@PathVariable Long id) {
        SmsAnalysisResponseDto response = smsAnalysisService.getAnalysisDetails(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/analysis/{id}")
    @Operation(summary = "Delete an analysis report from user history")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysis(@PathVariable Long id) {
        smsAnalysisService.deleteAnalysis(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Analysis record deleted successfully"));
    }

    @GetMapping("/fraud-categories")
    @Operation(summary = "Get list of supported fraud categories")
    public ResponseEntity<ApiResponse<List<String>>> getFraudCategories() {
        List<String> categories = List.of(
                "BANKING_FRAUD",
                "KYC_SCAM",
                "OTP_SCAM",
                "LOAN_SCAM",
                "INVESTMENT_SCAM",
                "JOB_SCAM",
                "DELIVERY_SCAM",
                "LOTTERY_SCAM",
                "IMPERSONATION",
                "UNKNOWN"
        );
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
