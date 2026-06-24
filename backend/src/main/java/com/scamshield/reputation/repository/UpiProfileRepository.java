package com.scamshield.reputation.repository;

import com.scamshield.reputation.entity.UpiProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpiProfileRepository extends JpaRepository<UpiProfile, Long> {
    Optional<UpiProfile> findByNormalizedUpi(String normalizedUpi);
}
