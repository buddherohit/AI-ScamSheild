package com.scamshield.reputation.repository;

import com.scamshield.reputation.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime timestamp);
}
