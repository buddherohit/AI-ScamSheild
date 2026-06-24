package com.scamshield.fraud.rule;

import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RecentFraudActivityStrategy implements FraudRuleStrategy {

    private final FraudReportRepository fraudReportRepository;

    @Override
    public String getRuleKey() {
        return "RULE_RECENT_FRAUD_ACTIVITY";
    }

    @Override
    public boolean evaluate(ReportedEntity entity, String type, String value) {
        if (entity == null) {
            return false;
        }
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return fraudReportRepository.existsByEntityIdAndStatusAndCreatedAtAfter(
                entity.getId(), 
                "APPROVED", 
                oneDayAgo
        );
    }
}
