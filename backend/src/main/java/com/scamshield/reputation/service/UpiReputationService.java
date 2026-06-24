package com.scamshield.reputation.service;

import com.scamshield.entity.User;
import com.scamshield.exception.BusinessException;
import com.scamshield.exception.ValidationException;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.ReportedEntityRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.repository.UserRepository;
import com.scamshield.reputation.dto.ReputationHistoryResponse;
import com.scamshield.reputation.dto.UpiVerifyRequest;
import com.scamshield.reputation.dto.UpiVerifyResponse;
import com.scamshield.reputation.entity.*;
import com.scamshield.reputation.repository.*;
import com.scamshield.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpiReputationService {

    private final UpiProfileRepository upiProfileRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final VerificationResultRepository verificationResultRepository;
    private final RiskReasonRepository riskReasonRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    private final UserRepository userRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final ReportedEntityRepository reportedEntityRepository;
    private final FraudReportRepository fraudReportRepository;
    private final AuditService auditService;

    private static final Pattern UPI_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$");
    private static final int RATE_LIMIT_MAX_PER_MINUTE = 30;

    @Transactional
    public UpiVerifyResponse verifyUpi(UpiVerifyRequest request, String ipAddress, String userAgent) {
        String rawUpi = request.getUpiId();
        if (rawUpi == null || rawUpi.trim().isEmpty()) {
            throw new ValidationException("UPI ID is required", Map.of("upiId", "UPI ID cannot be empty"));
        }

        // 1. Normalization
        String normalizedUpi = rawUpi.trim().toLowerCase();

        // 2. Format Validation
        if (!UPI_PATTERN.matcher(normalizedUpi).matches()) {
            throw new ValidationException("Invalid UPI ID format", Map.of("upiId", "UPI ID format is invalid. Must contain identifier and handle separated by @."));
        }

        // 3. Fetch current authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);

        // 4. Rate Limiting Check
        long requestCount = verificationRequestRepository.countByIpAddressAndCreatedAtAfter(
                ipAddress, LocalDateTime.now().minusMinutes(1));
        if (requestCount >= RATE_LIMIT_MAX_PER_MINUTE) {
            throw new BusinessException("Rate limit exceeded. Please wait a moment before verifying again.");
        }

        // 5. Audit Logging & Verification Request Save
        VerificationRequest verificationRequest = VerificationRequest.builder()
                .user(currentUser)
                .ipAddress(ipAddress)
                .entityType("UPI")
                .entityValue(normalizedUpi)
                .build();
        verificationRequestRepository.save(verificationRequest);

        // Audit log action
        if (currentUser != null) {
            auditService.logAction(currentUser.getId(), "VERIFY_UPI", ipAddress, userAgent);
        }

        // 6. Assess risk/reputation score
        List<String> reasons = new ArrayList<>();
        int score = 100; // Default is perfect reputation score (Trusted)

        // Rule A: Check blacklisted threat indicators
        boolean isThreat = threatIndicatorRepository.existsByValueAndIsActiveTrue(normalizedUpi);
        if (isThreat) {
            score = 0;
            reasons.add("Known Threat Indicator: Blacklisted by System");
        } else {
            // Rule B: Check active reports count
            ReportedEntity entity = reportedEntityRepository.findByTypeAndValue("upi", normalizedUpi).orElse(null);
            if (entity != null) {
                long approvedReportsCount = fraudReportRepository.countByEntityIdAndStatus(entity.getId(), "APPROVED");
                if (approvedReportsCount > 0) {
                    // Deduct 10 points per report up to 80 points
                    int deduction = Math.min(80, (int) (approvedReportsCount * 10));
                    score -= deduction;
                    reasons.add("Reported " + approvedReportsCount + " times in fraud database");

                    // Extra deduction for high severity reports
                    boolean hasHighSeverity = fraudReportRepository.existsByEntityIdAndSeverityInAndStatus(
                            entity.getId(), List.of("HIGH", "CRITICAL"), "APPROVED");
                    if (hasHighSeverity) {
                        score -= 20;
                        reasons.add("Linked to critical or high-severity approved reports");
                    }

                    // Extra deduction for recent activity (last 24 hours)
                    boolean hasRecent = fraudReportRepository.existsByEntityIdAndStatusAndCreatedAtAfter(
                            entity.getId(), "APPROVED", LocalDateTime.now().minusDays(1));
                    if (hasRecent) {
                        score -= 10;
                        reasons.add("Recent fraud activity reported in the last 24 hours");
                    }
                }
            }
        }

        // Ensure score bounds
        score = Math.max(0, Math.min(100, score));

        // Determine Status/Risk Level (Reputation score 0-100, where higher is better)
        String status;
        String riskLevel;
        String recommendation;
        if (score >= 90) {
            status = "TRUSTED";
            riskLevel = "LOW";
            recommendation = "Safe to proceed";
        } else if (score >= 70) {
            status = "SAFE";
            riskLevel = "LOW";
            recommendation = "Safe to proceed";
        } else if (score >= 40) {
            status = "SUSPICIOUS";
            riskLevel = "MEDIUM";
            recommendation = "Proceed with caution";
        } else {
            status = "DANGEROUS";
            riskLevel = "HIGH";
            recommendation = "Do not proceed";
        }

        if (reasons.isEmpty()) {
            reasons.add("No threat activity or reports found in database.");
        }

        // 7. Save/Update UPI Profile
        UpiProfile profile = upiProfileRepository.findByNormalizedUpi(normalizedUpi).orElse(null);
        if (profile == null) {
            profile = UpiProfile.builder()
                    .upiId(normalizedUpi)
                    .normalizedUpi(normalizedUpi)
                    .riskScore(100 - score) // riskScore is 100 - reputationScore
                    .riskLevel(riskLevel)
                    .build();
        } else {
            profile.setRiskScore(100 - score);
            profile.setRiskLevel(riskLevel);
        }
        upiProfileRepository.save(profile);

        // 8. Save Risk Reasons
        riskReasonRepository.deleteByEntityTypeAndEntityValue("UPI", normalizedUpi);
        for (String reason : reasons) {
            riskReasonRepository.save(RiskReason.builder()
                    .entityType("UPI")
                    .entityValue(normalizedUpi)
                    .reason(reason)
                    .build());
        }

        // 9. Save Verification Result snapshot
        VerificationResult result = VerificationResult.builder()
                .request(verificationRequest)
                .riskScore(100 - score)
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .build();
        verificationResultRepository.save(result);

        // 10. Save Reputation History log
        ReputationHistory history = ReputationHistory.builder()
                .user(currentUser)
                .verifiedEntity(normalizedUpi)
                .entityType("UPI")
                .riskScore(100 - score)
                .riskLevel(riskLevel)
                .status(status)
                .build();
        reputationHistoryRepository.save(history);

        return UpiVerifyResponse.builder()
                .upiId(normalizedUpi)
                .score(score)
                .status(status)
                .reasons(reasons)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReputationHistoryResponse> getUserHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return reputationHistoryRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(h -> ReputationHistoryResponse.builder()
                        .id(h.getId())
                        .verifiedEntity(h.getVerifiedEntity())
                        .entityType(h.getEntityType())
                        .riskScore(h.getRiskScore())
                        .riskLevel(h.getRiskLevel())
                        .status(h.getStatus())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UpiVerifyResponse getUpiScore(String upiId) {
        String normalizedUpi = upiId.trim().toLowerCase();
        UpiProfile profile = upiProfileRepository.findByNormalizedUpi(normalizedUpi).orElse(null);

        int score = 100;
        String status = "TRUSTED";
        List<String> reasons = new ArrayList<>();

        if (profile != null) {
            score = 100 - profile.getRiskScore();
            if (score >= 90) {
                status = "TRUSTED";
            } else if (score >= 70) {
                status = "SAFE";
            } else if (score >= 40) {
                status = "SUSPICIOUS";
            } else {
                status = "DANGEROUS";
            }

            reasons = riskReasonRepository.findByEntityTypeAndEntityValue("UPI", normalizedUpi).stream()
                    .map(RiskReason::getReason)
                    .collect(Collectors.toList());
        }

        if (reasons.isEmpty()) {
            reasons.add("No threat activity or reports found in database.");
        }

        return UpiVerifyResponse.builder()
                .upiId(normalizedUpi)
                .score(score)
                .status(status)
                .reasons(reasons)
                .build();
    }
}
