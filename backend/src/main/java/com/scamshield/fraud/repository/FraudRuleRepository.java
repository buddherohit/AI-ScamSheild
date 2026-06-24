package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {
    List<FraudRule> findAllByIsActiveTrue();
    Optional<FraudRule> findByRuleKey(String ruleKey);
}
