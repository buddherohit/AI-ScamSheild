package com.scamshield.fraud.rule;

import com.scamshield.fraud.entity.ReportedEntity;

public interface FraudRuleStrategy {
    /**
     * Get the unique rule key this strategy supports.
     */
    String getRuleKey();

    /**
     * Evaluate if the rule is triggered for the given entity.
     * 
     * @param entity the entity if already registered, or null
     * @param type the type of the entity (e.g. PHONE, UPI_ID)
     * @param value the value of the entity
     * @return true if the rule matches and should apply its weight
     */
    boolean evaluate(ReportedEntity entity, String type, String value);
}
