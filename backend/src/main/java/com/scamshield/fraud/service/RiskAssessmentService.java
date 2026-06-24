package com.scamshield.fraud.service;

import com.scamshield.fraud.dto.FraudVerifyRequest;
import com.scamshield.fraud.dto.FraudVerifyResponse;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.entity.RiskAssessment;
import com.scamshield.fraud.repository.ReportedEntityRepository;
import com.scamshield.fraud.repository.RiskAssessmentRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService {

    private final ReportedEntityRepository reportedEntityRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RuleEngineService ruleEngineService;

    @Transactional
    public FraudVerifyResponse assessRisk(FraudVerifyRequest request) {
        String type = request.getType();
        String value = request.getValue();

        List<String> reasons = new ArrayList<>();
        int score = 0;

        // 1. Check Threat Indicators Database (Immediate override)
        boolean isBlacklisted = threatIndicatorRepository.existsByValueAndIsActiveTrue(value);
        if (isBlacklisted) {
            score = 100;
            reasons.add("Known Scam Indicator: Blacklisted by System");
        } else {
            // 2. Fetch ReportedEntity if it exists
            ReportedEntity entity = reportedEntityRepository.findByTypeAndValue(type, value).orElse(null);

            // 3. Run Rule Engine
            RuleEngineService.RuleEvaluationResult ruleResult = ruleEngineService.evaluateRules(entity, type, value);
            score = ruleResult.getTotalScore();
            reasons.addAll(ruleResult.getReasons());
        }

        // Cap score at 0 - 100
        score = Math.max(0, Math.min(100, score));

        // Determine Level
        String level = getRiskLevelForScore(score);
        
        if (reasons.isEmpty()) {
            reasons.add("No threat activity or reports found in database.");
        }

        // Persist Risk Assessment Snapshot
        RiskAssessment assessment = RiskAssessment.builder()
                .entityType(type)
                .entityValue(value)
                .riskScore(score)
                .riskLevel(level)
                .reasons(String.join(", ", reasons))
                .build();
        riskAssessmentRepository.save(assessment);

        return FraudVerifyResponse.builder()
                .riskScore(score)
                .riskLevel(level)
                .reasons(reasons)
                .build();
    }

    public String getRiskLevelForScore(int score) {
        if (score <= 30) {
            return "SAFE";
        } else if (score <= 60) {
            return "MEDIUM";
        } else if (score <= 80) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }
}
