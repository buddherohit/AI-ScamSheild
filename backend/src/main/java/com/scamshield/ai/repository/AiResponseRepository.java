package com.scamshield.ai.repository;

import com.scamshield.ai.entity.AiResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiResponseRepository extends JpaRepository<AiResponse, Long> {
}
