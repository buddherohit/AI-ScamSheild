package com.scamshield.qr.service;

import com.scamshield.entity.User;
import com.scamshield.exception.BusinessException;
import com.scamshield.repository.UserRepository;
import com.scamshield.reputation.dto.UpiVerifyRequest;
import com.scamshield.reputation.dto.UpiVerifyResponse;
import com.scamshield.reputation.service.UpiReputationService;
import com.scamshield.qr.dto.QrScanResponse;
import com.scamshield.qr.entity.QrScan;
import com.scamshield.qr.repository.QrScanRepository;
import com.scamshield.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrIntelligenceService {

    private final QrScanRepository qrScanRepository;
    private final UserRepository userRepository;
    
    private final QRParser qrParser;
    private final UPIQRParser upiQrParser;
    private final MerchantQRParser merchantQrParser;
    
    private final UpiReputationService upiReputationService;
    private final AuditService auditService;

    @Transactional
    public QrScanResponse scanAndVerifyImage(byte[] qrImageBytes, String ipAddress, String userAgent) {
        String decodedText;
        try {
            decodedText = qrParser.parse(qrImageBytes);
        } catch (QrParsingException e) {
            log.warn("Failed to decode QR code image: {}", e.getMessage());
            throw new BusinessException("Failed to decode QR code from the uploaded image. Please ensure it is a valid QR code.");
        }

        return verifyRawText(decodedText, ipAddress, userAgent);
    }

    @Transactional
    public QrScanResponse verifyRawText(String rawText, String ipAddress, String userAgent) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new BusinessException("QR code text payload is empty");
        }

        // 1. Extract UPI ID and Merchant Name
        String extractedUpi = upiQrParser.extractUpiId(rawText);
        String extractedMerchant = merchantQrParser.extractMerchantName(rawText);

        if (extractedUpi == null) {
            throw new BusinessException("This QR code does not contain a valid UPI payment ID");
        }

        // 2. Invoke UPI Reputation Engine
        UpiVerifyResponse reputationResponse;
        try {
            UpiVerifyRequest verifyRequest = UpiVerifyRequest.builder()
                    .upiId(extractedUpi)
                    .build();
            reputationResponse = upiReputationService.verifyUpi(verifyRequest, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Failed to run reputation engine on extracted UPI: {}", extractedUpi, e);
            throw new BusinessException("Error running reputation engine on payment ID: " + e.getMessage());
        }

        // 3. Compute Risk Metrics (riskScore = 100 - reputationScore)
        int riskScore = 100 - reputationResponse.getScore();
        String riskLevel;
        String recommendation;

        if (riskScore <= 30) {
            riskLevel = "LOW";
            recommendation = "Safe to proceed";
        } else if (riskScore <= 60) {
            riskLevel = "MEDIUM";
            recommendation = "Proceed with caution";
        } else if (riskScore <= 80) {
            riskLevel = "HIGH";
            recommendation = "Do not proceed";
        } else {
            riskLevel = "CRITICAL";
            recommendation = "Do not proceed";
        }

        // 4. Retrieve logged-in user and save scan record
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);

        QrScan scan = QrScan.builder()
                .user(currentUser)
                .rawText(rawText)
                .extractedUpi(extractedUpi)
                .extractedMerchant(extractedMerchant)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .build();
        qrScanRepository.save(scan);

        // Audit scan
        if (currentUser != null) {
            auditService.logAction(currentUser.getId(), "SCAN_QR", ipAddress, userAgent);
        }

        return QrScanResponse.builder()
                .merchant(extractedMerchant)
                .upi(extractedUpi)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .build();
    }

    @Transactional(readOnly = true)
    public List<QrScanResponse> getScanHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return qrScanRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(s -> QrScanResponse.builder()
                        .merchant(s.getExtractedMerchant())
                        .upi(s.getExtractedUpi())
                        .riskScore(s.getRiskScore())
                        .riskLevel(s.getRiskLevel())
                        .recommendation(s.getRiskScore() >= 60 ? "Do not proceed" : (s.getRiskScore() >= 30 ? "Proceed with caution" : "Safe to proceed"))
                        .build())
                .collect(Collectors.toList());
    }
}
