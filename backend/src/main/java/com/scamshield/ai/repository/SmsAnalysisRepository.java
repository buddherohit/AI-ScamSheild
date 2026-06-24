package com.scamshield.ai.repository;

import com.scamshield.ai.entity.SmsAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsAnalysisRepository extends JpaRepository<SmsAnalysis, Long> {
    Page<SmsAnalysis> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT s FROM SmsAnalysis s WHERE s.user.id = :userId AND " +
           "(LOWER(s.smsText) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SmsAnalysis> searchByUserId(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);
}
