package com.scamshield.reputation.repository;

import com.scamshield.reputation.entity.ReputationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, Long> {
    List<ReputationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
