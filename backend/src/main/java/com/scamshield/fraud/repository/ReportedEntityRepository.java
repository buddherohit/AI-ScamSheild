package com.scamshield.fraud.repository;

import com.scamshield.fraud.entity.ReportedEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportedEntityRepository extends JpaRepository<ReportedEntity, Long> {
    Optional<ReportedEntity> findByTypeAndValue(String type, String value);
    Page<ReportedEntity> findByValueContainingIgnoreCase(String value, Pageable pageable);
}
