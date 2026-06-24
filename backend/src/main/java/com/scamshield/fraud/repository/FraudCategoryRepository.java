package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.FraudCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudCategoryRepository extends JpaRepository<FraudCategory, Long> {
    Optional<FraudCategory> findByCode(String code);
}
