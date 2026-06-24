package com.scamshield.fraud.rule;

import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class HighSeverityReportsStrategy implements FraudRuleStrategy {

    private final FraudReportRepository fraudReportRepository;

    @Override
    public String getRuleKey() {
        return "RULE_HIGH_SEVERITY_REPORTS";
    }

    @Override
    public boolean evaluate(ReportedEntity entity, String type, String value) {
        if (entity == null) {
            return false;
        }
        return fraudReportRepository.existsByEntityIdAndSeverityInAndStatus(
                entity.getId(), 
                Arrays.asList("HIGH", "CRITICAL"), 
                "APPROVED"
        );
    }
}
