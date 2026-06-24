package com.scamshield.unit.fraud;

import com.scamshield.fraud.dto.FraudVerifyRequest;
import com.scamshield.fraud.dto.FraudVerifyResponse;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.entity.RiskAssessment;
import com.scamshield.fraud.repository.ReportedEntityRepository;
import com.scamshield.fraud.repository.RiskAssessmentRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.fraud.service.RiskAssessmentService;
import com.scamshield.fraud.service.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {

    @Mock
    private ReportedEntityRepository reportedEntityRepository;

    @Mock
    private ThreatIndicatorRepository threatIndicatorRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private RuleEngineService ruleEngineService;

    private RiskAssessmentService riskAssessmentService;

    @BeforeEach
    void setUp() {
        riskAssessmentService = new RiskAssessmentService(
                reportedEntityRepository,
                threatIndicatorRepository,
                riskAssessmentRepository,
                ruleEngineService
        );
    }

    @Test
    void assessRisk_whenBlacklisted_shouldReturnCritical() {
        // Arrange
        FraudVerifyRequest request = new FraudVerifyRequest("PHONE", "9999999999");
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("9999999999")).thenReturn(true);

        // Act
        FraudVerifyResponse response = riskAssessmentService.assessRisk(request);

        // Assert
        assertEquals(100, response.getRiskScore());
        assertEquals("CRITICAL", response.getRiskLevel());
        assertTrue(response.getReasons().contains("Known Scam Indicator: Blacklisted by System"));
        verify(riskAssessmentRepository, times(1)).save(any(RiskAssessment.class));
    }

    @Test
    void assessRisk_whenNoThreat_shouldReturnSafe() {
        // Arrange
        FraudVerifyRequest request = new FraudVerifyRequest("PHONE", "1111111111");
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("1111111111")).thenReturn(false);
        when(reportedEntityRepository.findByTypeAndValue("PHONE", "1111111111")).thenReturn(Optional.empty());
        
        RuleEngineService.RuleEvaluationResult mockResult = new RuleEngineService.RuleEvaluationResult();
        when(ruleEngineService.evaluateRules(null, "PHONE", "1111111111")).thenReturn(mockResult);

        // Act
        FraudVerifyResponse response = riskAssessmentService.assessRisk(request);

        // Assert
        assertEquals(0, response.getRiskScore());
        assertEquals("SAFE", response.getRiskLevel());
        verify(riskAssessmentRepository, times(1)).save(any(RiskAssessment.class));
    }

    @Test
    void assessRisk_whenRulesTriggered_shouldAccumulateScore() {
        // Arrange
        FraudVerifyRequest request = new FraudVerifyRequest("EMAIL", "scam@domain.com");
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("scam@domain.com")).thenReturn(false);

        ReportedEntity mockEntity = ReportedEntity.builder()
                .type("EMAIL")
                .value("scam@domain.com")
                .isActive(true)
                .build();
        when(reportedEntityRepository.findByTypeAndValue("EMAIL", "scam@domain.com")).thenReturn(Optional.of(mockEntity));

        RuleEngineService.RuleEvaluationResult mockResult = new RuleEngineService.RuleEvaluationResult();
        mockResult.getReasons().add("Reported More Than 5 Times");
        mockResult.getReasons().add("Recent Fraud Activity");
        // Simulate score 35 (20 + 15)
        // Score 35 maps to MEDIUM
        try {
            java.lang.reflect.Field scoreField = RuleEngineService.RuleEvaluationResult.class.getDeclaredField("totalScore");
            scoreField.setAccessible(true);
            scoreField.setInt(mockResult, 35);
        } catch (Exception ignored) {}

        when(ruleEngineService.evaluateRules(mockEntity, "EMAIL", "scam@domain.com")).thenReturn(mockResult);

        // Act
        FraudVerifyResponse response = riskAssessmentService.assessRisk(request);

        // Assert
        assertEquals(35, response.getRiskScore());
        assertEquals("MEDIUM", response.getRiskLevel());
        assertTrue(response.getReasons().contains("Reported More Than 5 Times"));
        assertTrue(response.getReasons().contains("Recent Fraud Activity"));
        verify(riskAssessmentRepository, times(1)).save(any(RiskAssessment.class));
    }
}
