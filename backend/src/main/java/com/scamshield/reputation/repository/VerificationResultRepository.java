package com.scamshield.reputation.repository;

import com.scamshield.reputation.entity.VerificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationResultRepository extends JpaRepository<VerificationResult, Long> {
}
