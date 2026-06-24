package com.scamshield.fraud.service;

import com.scamshield.fraud.entity.FraudRule;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudRuleRepository;
import com.scamshield.fraud.rule.FraudRuleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleEngineService {

    private final FraudRuleRepository fraudRuleRepository;
    private final Map<String, FraudRuleStrategy> strategies;

    public RuleEngineService(FraudRuleRepository fraudRuleRepository, List<FraudRuleStrategy> strategyList) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FraudRuleStrategy::getRuleKey, Function.identity()));
    }

    public static class RuleEvaluationResult {
        private int totalScore;
        private final List<String> reasons = new ArrayList<>();

        public int getTotalScore() {
            return totalScore;
        }

        public List<String> getReasons() {
            return reasons;
        }
    }

    public RuleEvaluationResult evaluateRules(ReportedEntity entity, String type, String value) {
        RuleEvaluationResult result = new RuleEvaluationResult();
        List<FraudRule> activeRules = fraudRuleRepository.findAllByIsActiveTrue();

        for (FraudRule rule : activeRules) {
            FraudRuleStrategy strategy = strategies.get(rule.getRuleKey());
            if (strategy != null) {
                try {
                    if (strategy.evaluate(entity, type, value)) {
                        result.totalScore += rule.getWeight();
                        result.reasons.add(rule.getName());
                    }
                } catch (Exception e) {
                    log.error("Error evaluating rule strategy for rule key: {}", rule.getRuleKey(), e);
                }
            } else {
                log.warn("No strategy found for active rule key: {}", rule.getRuleKey());
            }
        }
        return result;
    }
}
