package com.scamshield.reputation.controller;

import com.scamshield.common.ApiResponse;
import com.scamshield.reputation.dto.ReputationHistoryResponse;
import com.scamshield.reputation.dto.UpiVerifyRequest;
import com.scamshield.reputation.dto.UpiVerifyResponse;
import com.scamshield.reputation.service.UpiReputationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reputation")
@RequiredArgsConstructor
@Tag(name = "UPI Reputation Engine", description = "Endpoints for checking UPI ID reputation scores and threat activity history")
public class UpiReputationController {

    private final UpiReputationService upiReputationService;

    @PostMapping("/verify-upi")
    @Operation(summary = "Verify a UPI ID and calculate its reputation score")
    public ResponseEntity<ApiResponse<UpiVerifyResponse>> verifyUpi(
            @Valid @RequestBody UpiVerifyRequest request,
            HttpServletRequest httpServletRequest) {
        
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }
        
        UpiVerifyResponse response = upiReputationService.verifyUpi(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "UPI verification completed successfully"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get the UPI verification history logs for the current user")
    public ResponseEntity<ApiResponse<List<ReputationHistoryResponse>>> getHistory() {
        List<ReputationHistoryResponse> history = upiReputationService.getUserHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/score/{upi}")
    @Operation(summary = "Get the current stored reputation score for a UPI ID")
    public ResponseEntity<ApiResponse<UpiVerifyResponse>> getScore(@PathVariable String upi) {
        UpiVerifyResponse response = upiReputationService.getUpiScore(upi);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
