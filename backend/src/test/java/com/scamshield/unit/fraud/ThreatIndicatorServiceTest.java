package com.scamshield.unit.fraud;

import com.scamshield.exception.BusinessException;
import com.scamshield.exception.ResourceNotFoundException;
import com.scamshield.fraud.dto.ThreatIndicatorRequest;
import com.scamshield.fraud.dto.ThreatIndicatorResponse;
import com.scamshield.fraud.entity.FraudAuditEvent;
import com.scamshield.fraud.entity.ThreatIndicator;
import com.scamshield.fraud.mapper.FraudMapper;
import com.scamshield.fraud.repository.FraudAuditEventRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.fraud.service.ThreatIndicatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThreatIndicatorServiceTest {

    @Mock
    private ThreatIndicatorRepository threatIndicatorRepository;

    @Mock
    private FraudAuditEventRepository auditEventRepository;

    @Mock
    private FraudMapper fraudMapper;

    @InjectMocks
    private ThreatIndicatorService threatIndicatorService;

    @Test
    void addIndicator_whenAlreadyExists_shouldThrowBusinessException() {
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("scam-value")).thenReturn(true);

        ThreatIndicatorRequest request = new ThreatIndicatorRequest();
        request.setValue("scam-value");

        assertThrows(BusinessException.class, () -> threatIndicatorService.addIndicator(request));
    }

    @Test
    void addIndicator_whenNew_shouldSaveAndAudit() {
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("new-scam-value")).thenReturn(false);

        ThreatIndicatorRequest request = new ThreatIndicatorRequest();
        request.setType("PHONE");
        request.setValue("new-scam-value");
        request.setSource("MANUAL");

        ThreatIndicator saved = ThreatIndicator.builder()
                .id(1L)
                .type("PHONE")
                .value("new-scam-value")
                .source("MANUAL")
                .isActive(true)
                .build();
        when(threatIndicatorRepository.save(any(ThreatIndicator.class))).thenReturn(saved);

        ThreatIndicatorResponse response = new ThreatIndicatorResponse();
        response.setId(1L);
        response.setType("PHONE");
        response.setValue("new-scam-value");
        response.setSource("MANUAL");
        response.setActive(true);
        when(fraudMapper.toThreatIndicatorResponse(saved)).thenReturn(response);

        ThreatIndicatorResponse result = threatIndicatorService.addIndicator(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.isActive());
        verify(threatIndicatorRepository, times(1)).save(any(ThreatIndicator.class));
        verify(auditEventRepository, times(1)).save(any(FraudAuditEvent.class));
    }

    @Test
    void updateIndicator_whenNotFound_shouldThrowResourceNotFoundException() {
        when(threatIndicatorRepository.findById(99L)).thenReturn(Optional.empty());

        ThreatIndicatorRequest request = new ThreatIndicatorRequest();
        assertThrows(ResourceNotFoundException.class, () -> threatIndicatorService.updateIndicator(99L, request));
    }

    @Test
    void updateIndicator_whenFound_shouldUpdateFields() {
        ThreatIndicator existing = ThreatIndicator.builder()
                .id(1L)
                .type("PHONE")
                .value("old-value")
                .source("SYSTEM")
                .isActive(true)
                .build();

        when(threatIndicatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(threatIndicatorRepository.save(any(ThreatIndicator.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudMapper.toThreatIndicatorResponse(any(ThreatIndicator.class))).thenAnswer(i -> {
            ThreatIndicator ti = i.getArgument(0);
            ThreatIndicatorResponse resp = new ThreatIndicatorResponse();
            resp.setId(ti.getId());
            resp.setType(ti.getType());
            resp.setValue(ti.getValue());
            resp.setSource(ti.getSource());
            resp.setActive(ti.isActive());
            return resp;
        });

        ThreatIndicatorRequest request = new ThreatIndicatorRequest();
        request.setType("EMAIL");
        request.setValue("new-value");
        request.setSource("ADMIN");

        ThreatIndicatorResponse result = threatIndicatorService.updateIndicator(1L, request);

        assertEquals("EMAIL", result.getType());
        assertEquals("new-value", result.getValue());
        assertEquals("ADMIN", result.getSource());
    }

    @Test
    void deactivateIndicator_whenNotFound_shouldThrowResourceNotFoundException() {
        when(threatIndicatorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> threatIndicatorService.deactivateIndicator(99L));
    }

    @Test
    void deactivateIndicator_whenFound_shouldSetInactiveAndAudit() {
        ThreatIndicator existing = ThreatIndicator.builder()
                .id(1L)
                .type("PHONE")
                .value("some-value")
                .isActive(true)
                .build();

        when(threatIndicatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(threatIndicatorRepository.save(any(ThreatIndicator.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudMapper.toThreatIndicatorResponse(any(ThreatIndicator.class))).thenAnswer(i -> {
            ThreatIndicator ti = i.getArgument(0);
            ThreatIndicatorResponse resp = new ThreatIndicatorResponse();
            resp.setId(ti.getId());
            resp.setActive(ti.isActive());
            return resp;
        });

        ThreatIndicatorResponse result = threatIndicatorService.deactivateIndicator(1L);
        assertFalse(result.isActive());
        verify(auditEventRepository, times(1)).save(any(FraudAuditEvent.class));
    }

    @Test
    void searchIndicators_whenEmptyQuery_shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        ThreatIndicator ti = new ThreatIndicator();
        Page<ThreatIndicator> page = new PageImpl<>(Collections.singletonList(ti));
        when(threatIndicatorRepository.findAll(pageable)).thenReturn(page);
        when(fraudMapper.toThreatIndicatorResponse(ti)).thenReturn(new ThreatIndicatorResponse());

        Page<ThreatIndicatorResponse> result = threatIndicatorService.searchIndicators("", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchIndicators_withQuery_shouldSearchByQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        ThreatIndicator ti = new ThreatIndicator();
        Page<ThreatIndicator> page = new PageImpl<>(Collections.singletonList(ti));
        when(threatIndicatorRepository.findByValueContainingIgnoreCase("scam", pageable)).thenReturn(page);
        when(fraudMapper.toThreatIndicatorResponse(ti)).thenReturn(new ThreatIndicatorResponse());

        Page<ThreatIndicatorResponse> result = threatIndicatorService.searchIndicators("scam", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void isThreat_shouldCheckRepository() {
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("some-scam")).thenReturn(true);
        assertTrue(threatIndicatorService.isThreat("some-scam"));

        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("safe")).thenReturn(false);
        assertFalse(threatIndicatorService.isThreat("safe"));
    }
}
