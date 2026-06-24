package com.scamshield.reputation.repository;

import com.scamshield.reputation.entity.RiskReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskReasonRepository extends JpaRepository<RiskReason, Long> {
    List<RiskReason> findByEntityTypeAndEntityValue(String entityType, String entityValue);
    void deleteByEntityTypeAndEntityValue(String entityType, String entityValue);
}
