package com.scamshield.unit.fraud;

import com.scamshield.entity.User;
import com.scamshield.exception.ResourceNotFoundException;
import com.scamshield.exception.UnauthorizedException;
import com.scamshield.fraud.dto.FraudReportRequest;
import com.scamshield.fraud.dto.FraudReportResponse;
import com.scamshield.fraud.entity.FraudAuditEvent;
import com.scamshield.fraud.entity.FraudCategory;
import com.scamshield.fraud.entity.FraudReport;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.mapper.FraudMapper;
import com.scamshield.fraud.repository.FraudAuditEventRepository;
import com.scamshield.fraud.repository.FraudCategoryRepository;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.ReportedEntityRepository;
import com.scamshield.fraud.service.FraudReportService;
import com.scamshield.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudReportServiceTest {

    @Mock
    private FraudReportRepository fraudReportRepository;

    @Mock
    private ReportedEntityRepository reportedEntityRepository;

    @Mock
    private FraudCategoryRepository fraudCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FraudAuditEventRepository auditEventRepository;

    @Mock
    private FraudMapper fraudMapper;

    @InjectMocks
    private FraudReportService fraudReportService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createReport_whenUserNotAuthorized_shouldThrowUnauthorizedException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@scamshield.com");
        when(userRepository.findByEmail("test@scamshield.com")).thenReturn(Optional.empty());

        FraudReportRequest request = new FraudReportRequest();
        request.setCategoryCode("PHISHING");

        assertThrows(UnauthorizedException.class, () -> fraudReportService.createReport(request));
    }

    @Test
    void createReport_whenInvalidCategory_shouldThrowResourceNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@scamshield.com");
        
        User reporter = new User();
        reporter.setEmail("test@scamshield.com");
        when(userRepository.findByEmail("test@scamshield.com")).thenReturn(Optional.of(reporter));
        when(fraudCategoryRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        FraudReportRequest request = new FraudReportRequest();
        request.setCategoryCode("INVALID");

        assertThrows(ResourceNotFoundException.class, () -> fraudReportService.createReport(request));
    }

    @Test
    void createReport_whenSuccessful_shouldCreateReportAndAudit() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@scamshield.com");

        User reporter = new User();
        reporter.setEmail("test@scamshield.com");
        when(userRepository.findByEmail("test@scamshield.com")).thenReturn(Optional.of(reporter));

        FraudCategory category = new FraudCategory();
        category.setCode("PHISHING");
        when(fraudCategoryRepository.findByCode("PHISHING")).thenReturn(Optional.of(category));

        ReportedEntity entity = ReportedEntity.builder().id(1L).type("PHONE").value("12345").build();
        when(reportedEntityRepository.findByTypeAndValue("PHONE", "12345")).thenReturn(Optional.of(entity));

        FraudReportRequest request = new FraudReportRequest();
        request.setCategoryCode("PHISHING");
        request.setEntityType("PHONE");
        request.setEntityValue("12345");
        request.setDescription("Phishing attempt via SMS");
        request.setSeverity("HIGH");

        FraudReport savedReport = new FraudReport();
        savedReport.setId(10L);
        savedReport.setEntity(entity);
        savedReport.setCategory(category);
        savedReport.setReporter(reporter);
        savedReport.setDescription(request.getDescription());
        savedReport.setSeverity(request.getSeverity());
        savedReport.setStatus("PENDING");

        when(fraudReportRepository.save(any(FraudReport.class))).thenReturn(savedReport);

        FraudReportResponse responseDto = new FraudReportResponse();
        responseDto.setId(10L);
        responseDto.setEntityType("PHONE");
        responseDto.setEntityValue("12345");
        responseDto.setCategoryCode("PHISHING");
        responseDto.setSeverity("HIGH");
        responseDto.setStatus("PENDING");

        when(fraudMapper.toReportResponse(savedReport)).thenReturn(responseDto);

        FraudReportResponse result = fraudReportService.createReport(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("PENDING", result.getStatus());
        verify(fraudReportRepository, times(1)).save(any(FraudReport.class));
        verify(auditEventRepository, times(1)).save(any(FraudAuditEvent.class));
    }

    @Test
    void getReportById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(fraudReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fraudReportService.getReportById(99L));
    }

    @Test
    void getReportById_whenFound_shouldReturnResponse() {
        FraudReport report = new FraudReport();
        report.setId(1L);
        when(fraudReportRepository.findById(1L)).thenReturn(Optional.of(report));

        FraudReportResponse response = new FraudReportResponse();
        response.setId(1L);
        when(fraudMapper.toReportResponse(report)).thenReturn(response);

        FraudReportResponse result = fraudReportService.getReportById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void searchReports_whenValueEmpty_shouldFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        FraudReport report = new FraudReport();
        Page<FraudReport> page = new PageImpl<>(Collections.singletonList(report));
        when(fraudReportRepository.findAll(pageable)).thenReturn(page);
        when(fraudMapper.toReportResponse(report)).thenReturn(new FraudReportResponse());

        Page<FraudReportResponse> result = fraudReportService.searchReports("", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchReports_whenValueProvided_shouldSearchByValue() {
        Pageable pageable = PageRequest.of(0, 10);
        FraudReport report = new FraudReport();
        Page<FraudReport> page = new PageImpl<>(Collections.singletonList(report));
        when(fraudReportRepository.findByEntityValueContainingIgnoreCase("scam", pageable)).thenReturn(page);
        when(fraudMapper.toReportResponse(report)).thenReturn(new FraudReportResponse());

        Page<FraudReportResponse> result = fraudReportService.searchReports("scam", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void approveReport_shouldUpdateStatusAndCreateAudit() {
        FraudReport report = new FraudReport();
        report.setId(1L);
        report.setStatus("PENDING");

        when(fraudReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(fraudReportRepository.save(any(FraudReport.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudMapper.toReportResponse(any(FraudReport.class))).thenAnswer(i -> {
            FraudReport r = i.getArgument(0);
            FraudReportResponse resp = new FraudReportResponse();
            resp.setId(r.getId());
            resp.setStatus(r.getStatus());
            return resp;
        });

        FraudReportResponse result = fraudReportService.approveReport(1L);
        assertEquals("APPROVED", result.getStatus());
        verify(auditEventRepository, times(1)).save(any(FraudAuditEvent.class));
    }

    @Test
    void rejectReport_shouldUpdateStatusAndCreateAudit() {
        FraudReport report = new FraudReport();
        report.setId(1L);
        report.setStatus("PENDING");

        when(fraudReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(fraudReportRepository.save(any(FraudReport.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudMapper.toReportResponse(any(FraudReport.class))).thenAnswer(i -> {
            FraudReport r = i.getArgument(0);
            FraudReportResponse resp = new FraudReportResponse();
            resp.setId(r.getId());
            resp.setStatus(r.getStatus());
            return resp;
        });

        FraudReportResponse result = fraudReportService.rejectReport(1L);
        assertEquals("REJECTED", result.getStatus());
        verify(auditEventRepository, times(1)).save(any(FraudAuditEvent.class));
    }

    @Test
    void flagReport_shouldUpdateStatusAndCreateAudit() {
        FraudReport report = new FraudReport();
        report.setId(1L);
        report.setStatus("PENDING");

        when(fraudReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(fraudReportRepository.save(any(FraudReport.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudMapper.toReportResponse(any(FraudReport.class))).thenAnswer(i -> {
            FraudReport r = i.getArgument(0);
            FraudReportResponse resp = new FraudReportResponse();
            resp.setId(r.getId());
            resp.setStatus(r.getStatus());
            return resp;
        });

        FraudReportResponse result = fraudReportService.flagReport(1L);
        assertEquals("FLAGGED", result.getStatus());
        verify(auditEventRepository, times(1)).save(any(FraudAuditEvent.class));
    }
}
