package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.FraudReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface FraudReportRepository extends JpaRepository<FraudReport, Long> {
    long countByEntityIdAndStatus(Long entityId, String status);
    
    boolean existsByEntityIdAndSeverityInAndStatus(Long entityId, Collection<String> severities, String status);
    
    boolean existsByEntityIdAndStatusAndCreatedAtAfter(Long entityId, String status, LocalDateTime time);
    
    Page<FraudReport> findByEntityValueContainingIgnoreCase(String value, Pageable pageable);
    
    long countByStatus(String status);
}
