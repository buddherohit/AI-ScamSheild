package com.scamshield.ai.repository;

import com.scamshield.ai.entity.FraudPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudPatternRepository extends JpaRepository<FraudPattern, Long> {
    Optional<FraudPattern> findByPatternKey(String patternKey);
}
