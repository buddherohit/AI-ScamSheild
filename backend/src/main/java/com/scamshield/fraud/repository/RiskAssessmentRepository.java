package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.RiskAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    Page<RiskAssessment> findByEntityValueContainingIgnoreCase(String value, Pageable pageable);
    List<RiskAssessment> findTop10ByOrderByCreatedAtDesc();
    long countByRiskLevel(String riskLevel);
}
