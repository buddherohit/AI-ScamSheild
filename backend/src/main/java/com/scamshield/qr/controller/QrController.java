package com.scamshield.qr.controller;

import com.scamshield.common.ApiResponse;
import com.scamshield.qr.dto.QrScanResponse;
import com.scamshield.qr.dto.QrVerifyRequest;
import com.scamshield.qr.service.QrIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
@Tag(name = "QR Code Intelligence", description = "Endpoints for uploading, parsing, and verifying UPI QR codes")
public class QrController {

    private final QrIntelligenceService qrIntelligenceService;

    @PostMapping("/scan")
    @Operation(summary = "Upload a QR code image to parse and verify the destination payment profile")
    public ResponseEntity<ApiResponse<QrScanResponse>> scanQr(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest) {
        
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }

        try {
            byte[] bytes = file.getBytes();
            QrScanResponse response = qrIntelligenceService.scanAndVerifyImage(bytes, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success(response, "QR code image scanned and verified"));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to read uploaded file"));
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify raw text contents parsed from a QR code")
    public ResponseEntity<ApiResponse<QrScanResponse>> verifyQrText(
            @Valid @RequestBody QrVerifyRequest request,
            HttpServletRequest httpServletRequest) {
        
        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "Unknown";
        }

        QrScanResponse response = qrIntelligenceService.verifyRawText(request.getRawText(), ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "QR raw payload verified"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get QR scan history for the current authenticated user")
    public ResponseEntity<ApiResponse<List<QrScanResponse>>> getHistory() {
        List<QrScanResponse> history = qrIntelligenceService.getScanHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
