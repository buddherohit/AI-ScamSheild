package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.ThreatIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ThreatIndicatorRepository extends JpaRepository<ThreatIndicator, Long> {
    Optional<ThreatIndicator> findByTypeAndValue(String type, String value);
    Optional<ThreatIndicator> findByValue(String value);
    Page<ThreatIndicator> findByValueContainingIgnoreCase(String value, Pageable pageable);
    boolean existsByValueAndIsActiveTrue(String value);
    
    // Check if any active threat indicators were created after a given timestamp (recent activity check)
    boolean existsByIsActiveTrueAndCreatedAtAfter(LocalDateTime time);
}
