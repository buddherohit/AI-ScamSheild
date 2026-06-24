package com.scamshield.ai.repository;

import com.scamshield.ai.entity.AiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiRequestRepository extends JpaRepository<AiRequest, Long> {
}
