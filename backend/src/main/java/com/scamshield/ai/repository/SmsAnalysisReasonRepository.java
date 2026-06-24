package com.scamshield.ai.repository;

import com.scamshield.ai.entity.SmsAnalysisReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsAnalysisReasonRepository extends JpaRepository<SmsAnalysisReason, Long> {
    List<SmsAnalysisReason> findByAnalysisId(Long analysisId);
}
