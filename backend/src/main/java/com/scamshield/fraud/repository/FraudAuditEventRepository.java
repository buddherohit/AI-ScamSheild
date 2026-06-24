package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.FraudAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAuditEventRepository extends JpaRepository<FraudAuditEvent, Long> {
    List<FraudAuditEvent> findTop10ByOrderByCreatedAtDesc();
}
