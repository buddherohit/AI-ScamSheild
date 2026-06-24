package com.scamshield.unit.fraud;

import com.scamshield.fraud.entity.FraudRule;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.FraudRuleRepository;
import com.scamshield.fraud.rule.*;
import com.scamshield.fraud.service.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private FraudReportRepository fraudReportRepository;

    private RuleEngineService ruleEngineService;

    private ReportCountGt5Strategy countGt5Strategy;
    private ReportCountGt20Strategy countGt20Strategy;
    private RecentFraudActivityStrategy recentActivityStrategy;
    private HighSeverityReportsStrategy highSeverityStrategy;

    @BeforeEach
    void setUp() {
        countGt5Strategy = new ReportCountGt5Strategy(fraudReportRepository);
        countGt20Strategy = new ReportCountGt20Strategy(fraudReportRepository);
        recentActivityStrategy = new RecentFraudActivityStrategy(fraudReportRepository);
        highSeverityStrategy = new HighSeverityReportsStrategy(fraudReportRepository);

        List<FraudRuleStrategy> strategies = Arrays.asList(
                countGt5Strategy,
                countGt20Strategy,
                recentActivityStrategy,
                highSeverityStrategy
        );

        ruleEngineService = new RuleEngineService(fraudRuleRepository, strategies);
    }

    @Test
    void testRuleEngine_whenNoActiveRules_shouldReturnEmptyResult() {
        when(fraudRuleRepository.findAllByIsActiveTrue()).thenReturn(Collections.emptyList());

        RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(null, "PHONE", "12345");

        assertEquals(0, result.getTotalScore());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void testRuleEngine_whenRulesTriggered_shouldAccumulateScoreAndReasons() {
        ReportedEntity entity = ReportedEntity.builder().id(1L).type("PHONE").value("12345").build();

        FraudRule rule1 = FraudRule.builder()
                .ruleKey("RULE_REPORT_COUNT_GT_5")
                .name("Reported More Than 5 Times")
                .weight(20)
                .isActive(true)
                .build();

        FraudRule rule2 = FraudRule.builder()
                .ruleKey("RULE_HIGH_SEVERITY_REPORTS")
                .name("High Severity Reports")
                .weight(30)
                .isActive(true)
                .build();

        when(fraudRuleRepository.findAllByIsActiveTrue()).thenReturn(Arrays.asList(rule1, rule2));

        // Mock strategy evaluations
        when(fraudReportRepository.countByEntityIdAndStatus(1L, "APPROVED")).thenReturn(6L);
        when(fraudReportRepository.existsByEntityIdAndSeverityInAndStatus(eq(1L), any(), eq("APPROVED"))).thenReturn(true);

        RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(entity, "PHONE", "12345");

        assertEquals(50, result.getTotalScore());
        assertEquals(2, result.getReasons().size());
        assertTrue(result.getReasons().contains("Reported More Than 5 Times"));
        assertTrue(result.getReasons().contains("High Severity Reports"));
    }

    @Test
    void testReportCountGt5Strategy() {
        ReportedEntity entity = ReportedEntity.builder().id(2L).build();

        // null entity
        assertFalse(countGt5Strategy.evaluate(null, "PHONE", "12345"));

        // count <= 5
        when(fraudReportRepository.countByEntityIdAndStatus(2L, "APPROVED")).thenReturn(5L);
        assertFalse(countGt5Strategy.evaluate(entity, "PHONE", "12345"));

        // count > 5
        when(fraudReportRepository.countByEntityIdAndStatus(2L, "APPROVED")).thenReturn(6L);
        assertTrue(countGt5Strategy.evaluate(entity, "PHONE", "12345"));
    }

    @Test
    void testReportCountGt20Strategy() {
        ReportedEntity entity = ReportedEntity.builder().id(3L).build();

        // null entity
        assertFalse(countGt20Strategy.evaluate(null, "PHONE", "12345"));

        // count <= 20
        when(fraudReportRepository.countByEntityIdAndStatus(3L, "APPROVED")).thenReturn(20L);
        assertFalse(countGt20Strategy.evaluate(entity, "PHONE", "12345"));

        // count > 20
        when(fraudReportRepository.countByEntityIdAndStatus(3L, "APPROVED")).thenReturn(21L);
        assertTrue(countGt20Strategy.evaluate(entity, "PHONE", "12345"));
    }

    @Test
    void testRecentFraudActivityStrategy() {
        ReportedEntity entity = ReportedEntity.builder().id(4L).build();

        // null entity
        assertFalse(recentActivityStrategy.evaluate(null, "PHONE", "12345"));

        // not exists
        when(fraudReportRepository.existsByEntityIdAndStatusAndCreatedAtAfter(eq(4L), eq("APPROVED"), any(LocalDateTime.class)))
                .thenReturn(false);
        assertFalse(recentActivityStrategy.evaluate(entity, "PHONE", "12345"));

        // exists
        when(fraudReportRepository.existsByEntityIdAndStatusAndCreatedAtAfter(eq(4L), eq("APPROVED"), any(LocalDateTime.class)))
                .thenReturn(true);
        assertTrue(recentActivityStrategy.evaluate(entity, "PHONE", "12345"));
    }

    @Test
    void testHighSeverityReportsStrategy() {
        ReportedEntity entity = ReportedEntity.builder().id(5L).build();

        // null entity
        assertFalse(highSeverityStrategy.evaluate(null, "PHONE", "12345"));

        // not exists
        when(fraudReportRepository.existsByEntityIdAndSeverityInAndStatus(eq(5L), any(), eq("APPROVED")))
                .thenReturn(false);
        assertFalse(highSeverityStrategy.evaluate(entity, "PHONE", "12345"));

        // exists
        when(fraudReportRepository.existsByEntityIdAndSeverityInAndStatus(eq(5L), any(), eq("APPROVED")))
                .thenReturn(true);
        assertTrue(highSeverityStrategy.evaluate(entity, "PHONE", "12345"));
    }
}
