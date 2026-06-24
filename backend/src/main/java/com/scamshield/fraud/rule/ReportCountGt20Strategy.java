package com.scamshield.fraud.rule;

import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportCountGt20Strategy implements FraudRuleStrategy {

    private final FraudReportRepository fraudReportRepository;

    @Override
    public String getRuleKey() {
        return "RULE_REPORT_COUNT_GT_20";
    }

    @Override
    public boolean evaluate(ReportedEntity entity, String type, String value) {
        if (entity == null) {
            return false;
        }
        long count = fraudReportRepository.countByEntityIdAndStatus(entity.getId(), "APPROVED");
        return count > 20;
    }
}
