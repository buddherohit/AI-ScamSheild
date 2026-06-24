package com.scamshield.reputation.repository;

import com.scamshield.reputation.entity.MerchantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, Long> {
    Optional<MerchantProfile> findByUpiProfileId(Long upiProfileId);
}
