package com.scamshield.unit.ai;

import com.scamshield.ai.entity.FraudPattern;
import com.scamshield.ai.repository.FraudPatternRepository;
import com.scamshield.ai.service.FraudPatternEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudPatternEngineTest {

    @Mock
    private FraudPatternRepository fraudPatternRepository;

    @InjectMocks
    private FraudPatternEngine fraudPatternEngine;

    private FraudPattern otpPattern;
    private FraudPattern kycPattern;

    @BeforeEach
    void setUp() {
        otpPattern = FraudPattern.builder()
                .id(1L)
                .patternKey("OTP_REQUEST")
                .patternRegex("(?i)(otp|one-time password|verification code)")
                .category("OTP_SCAM")
                .weight(40)
                .build();

        kycPattern = FraudPattern.builder()
                .id(2L)
                .patternKey("KYC_SUSPENDED")
                .patternRegex("(?i)(kyc|verify PAN|suspended|blocked)")
                .category("KYC_SCAM")
                .weight(50)
                .build();
    }

    @Test
    void evaluateText_withNoMatches_shouldReturnUnknownAndZeroScore() {
        when(fraudPatternRepository.findAll()).thenReturn(List.of(otpPattern, kycPattern));

        FraudPatternEngine.PatternResult result = fraudPatternEngine.evaluateText("Hello, what are you doing today?");
        
        assertEquals(0, result.getRuleScore());
        assertEquals("UNKNOWN", result.getDominantCategory());
        assertTrue(result.getMatchedIndicators().isEmpty());
    }

    @Test
    void evaluateText_withOtpMatch_shouldReturnOtpDetails() {
        when(fraudPatternRepository.findAll()).thenReturn(List.of(otpPattern, kycPattern));

        FraudPatternEngine.PatternResult result = fraudPatternEngine.evaluateText("Do not share your verification code with anyone.");
        
        assertEquals(40, result.getRuleScore());
        assertEquals("OTP_SCAM", result.getDominantCategory());
        assertEquals(List.of("OTP_REQUEST"), result.getMatchedIndicators());
    }

    @Test
    void evaluateText_withMultipleMatches_shouldAggregateScoreAndChooseDominantCategory() {
        when(fraudPatternRepository.findAll()).thenReturn(List.of(otpPattern, kycPattern));

        // Matches both OTP (40) and KYC (50) -> total score 90. Dominant category is KYC_SCAM since weight 50 > 40.
        FraudPatternEngine.PatternResult result = fraudPatternEngine.evaluateText("URGENT: Your KYC is suspended. Input OTP code to verify.");
        
        assertEquals(90, result.getRuleScore());
        assertEquals("KYC_SCAM", result.getDominantCategory());
        assertTrue(result.getMatchedIndicators().contains("OTP_REQUEST"));
        assertTrue(result.getMatchedIndicators().contains("KYC_SUSPENDED"));
    }

    @Test
    void evaluateText_withEmptyText_shouldReturnEmptyResult() {
        FraudPatternEngine.PatternResult result1 = fraudPatternEngine.evaluateText("");
        assertEquals(0, result1.getRuleScore());
        assertEquals("UNKNOWN", result1.getDominantCategory());

        FraudPatternEngine.PatternResult result2 = fraudPatternEngine.evaluateText(null);
        assertEquals(0, result2.getRuleScore());
        assertEquals("UNKNOWN", result2.getDominantCategory());
    }
}
