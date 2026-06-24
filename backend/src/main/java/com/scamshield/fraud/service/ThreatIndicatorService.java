package com.scamshield.fraud.service;

import com.scamshield.exception.BusinessException;
import com.scamshield.exception.ResourceNotFoundException;
import com.scamshield.fraud.dto.ThreatIndicatorRequest;
import com.scamshield.fraud.dto.ThreatIndicatorResponse;
import com.scamshield.fraud.entity.FraudAuditEvent;
import com.scamshield.fraud.entity.ThreatIndicator;
import com.scamshield.fraud.mapper.FraudMapper;
import com.scamshield.fraud.repository.FraudAuditEventRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreatIndicatorService {

    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final FraudAuditEventRepository auditEventRepository;
    private final FraudMapper fraudMapper;

    @Transactional
    public ThreatIndicatorResponse addIndicator(ThreatIndicatorRequest request) {
        if (threatIndicatorRepository.existsByValueAndIsActiveTrue(request.getValue())) {
            throw new BusinessException("Active threat indicator with this value already exists");
        }

        ThreatIndicator indicator = ThreatIndicator.builder()
                .type(request.getType())
                .value(request.getValue())
                .source(request.getSource() != null ? request.getSource() : "ADMIN")
                .isActive(true)
                .build();

        ThreatIndicator saved = threatIndicatorRepository.save(indicator);

        auditEventRepository.save(FraudAuditEvent.builder()
                .eventType("THREAT_ADDED")
                .entityId(saved.getId().toString())
                .details("Added threat indicator of type " + saved.getType() + " with value: " + saved.getValue())
                .build());

        return fraudMapper.toThreatIndicatorResponse(saved);
    }

    @Transactional
    public ThreatIndicatorResponse updateIndicator(Long id, ThreatIndicatorRequest request) {
        ThreatIndicator indicator = threatIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threat indicator not found"));

        indicator.setType(request.getType());
        indicator.setValue(request.getValue());
        if (request.getSource() != null) {
            indicator.setSource(request.getSource());
        }

        ThreatIndicator saved = threatIndicatorRepository.save(indicator);
        return fraudMapper.toThreatIndicatorResponse(saved);
    }

    @Transactional
    public ThreatIndicatorResponse deactivateIndicator(Long id) {
        ThreatIndicator indicator = threatIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threat indicator not found"));

        indicator.setActive(false);
        ThreatIndicator saved = threatIndicatorRepository.save(indicator);

        auditEventRepository.save(FraudAuditEvent.builder()
                .eventType("THREAT_DEACTIVATED")
                .entityId(saved.getId().toString())
                .details("Deactivated threat indicator of type " + saved.getType() + " with value: " + saved.getValue())
                .build());

        return fraudMapper.toThreatIndicatorResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ThreatIndicatorResponse> searchIndicators(String value, Pageable pageable) {
        Page<ThreatIndicator> page;
        if (value == null || value.trim().isEmpty()) {
            page = threatIndicatorRepository.findAll(pageable);
        } else {
            page = threatIndicatorRepository.findByValueContainingIgnoreCase(value, pageable);
        }
        return page.map(fraudMapper::toThreatIndicatorResponse);
    }

    @Transactional(readOnly = true)
    public boolean isThreat(String value) {
        return threatIndicatorRepository.existsByValueAndIsActiveTrue(value);
    }
}
