package com.scamshield.unit.fraud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.common.ApiResponse;
import com.scamshield.fraud.controller.FraudController;
import com.scamshield.fraud.dto.*;
import com.scamshield.fraud.entity.FraudCategory;
import com.scamshield.fraud.repository.FraudCategoryRepository;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.RiskAssessmentRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.fraud.service.FraudReportService;
import com.scamshield.fraud.service.RiskAssessmentService;
import com.scamshield.fraud.service.ThreatIndicatorService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FraudControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FraudReportService fraudReportService;

    @Mock
    private ThreatIndicatorService threatIndicatorService;

    @Mock
    private RiskAssessmentService riskAssessmentService;

    @Mock
    private FraudCategoryRepository fraudCategoryRepository;

    @Mock
    private FraudReportRepository fraudReportRepository;

    @Mock
    private ThreatIndicatorRepository threatIndicatorRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @InjectMocks
    private FraudController fraudController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fraudController).build();
    }

    @Test
    void submitReport_shouldReturnSuccess() throws Exception {
        FraudReportRequest request = new FraudReportRequest();
        request.setCategoryCode("PHISHING");
        request.setEntityType("EMAIL");
        request.setEntityValue("scam@attacker.com");
        request.setSeverity("HIGH");
        request.setDescription("Phishing email detected");

        FraudReportResponse response = new FraudReportResponse();
        response.setId(1L);
        response.setCategoryCode("PHISHING");

        when(fraudReportService.createReport(any(FraudReportRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fraud report submitted successfully"));
    }

    @Test
    void getReport_shouldReturnSuccess() throws Exception {
        FraudReportResponse response = new FraudReportResponse();
        response.setId(10L);

        when(fraudReportService.getReportById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/fraud/report/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void getReports_shouldReturnPage() throws Exception {
        Page<FraudReportResponse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(fraudReportService.searchReports(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/fraud/reports?search=test&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void verifyEntity_shouldReturnRiskAssessment() throws Exception {
        FraudVerifyRequest request = new FraudVerifyRequest("PHONE", "12345");
        FraudVerifyResponse response = FraudVerifyResponse.builder()
                .riskScore(50)
                .riskLevel("MEDIUM")
                .reasons(Arrays.asList("Reason"))
                .build();

        when(riskAssessmentService.assessRisk(any(FraudVerifyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskScore").value(50));
    }

    @Test
    void getCategories_shouldReturnList() throws Exception {
        FraudCategory cat = new FraudCategory();
        cat.setCode("PHISHING");
        when(fraudCategoryRepository.findAll()).thenReturn(Collections.singletonList(cat));

        mockMvc.perform(get("/api/v1/fraud/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("PHISHING"));
    }

    @Test
    void getStatistics_shouldReturnSummary() throws Exception {
        when(fraudReportRepository.count()).thenReturn(150L);
        when(riskAssessmentRepository.countByRiskLevel("CRITICAL")).thenReturn(5L);
        when(riskAssessmentRepository.countByRiskLevel("HIGH")).thenReturn(10L);
        when(threatIndicatorRepository.count()).thenReturn(20L);
        when(fraudReportRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0));

        mockMvc.perform(get("/api/v1/fraud/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metrics.totalThreatsBlocked").value(170))
                .andExpect(jsonPath("$.data.metrics.activeAlerts").value(15));
    }

    @Test
    void approveReport_shouldReturnReport() throws Exception {
        FraudReportResponse response = new FraudReportResponse();
        response.setId(1L);
        response.setStatus("APPROVED");

        when(fraudReportService.approveReport(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/report/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void rejectReport_shouldReturnReport() throws Exception {
        FraudReportResponse response = new FraudReportResponse();
        response.setId(1L);
        response.setStatus("REJECTED");

        when(fraudReportService.rejectReport(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/report/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void flagReport_shouldReturnReport() throws Exception {
        FraudReportResponse response = new FraudReportResponse();
        response.setId(1L);
        response.setStatus("FLAGGED");

        when(fraudReportService.flagReport(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/report/1/flag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FLAGGED"));
    }

    @Test
    void addIndicator_shouldReturnIndicator() throws Exception {
        ThreatIndicatorRequest request = new ThreatIndicatorRequest();
        request.setType("PHONE");
        request.setValue("12345678");

        ThreatIndicatorResponse response = new ThreatIndicatorResponse();
        response.setId(1L);
        response.setValue("12345678");

        when(threatIndicatorService.addIndicator(any(ThreatIndicatorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/indicator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.value").value("12345678"));
    }

    @Test
    void updateIndicator_shouldReturnIndicator() throws Exception {
        ThreatIndicatorRequest request = new ThreatIndicatorRequest();
        request.setType("EMAIL");
        request.setValue("scam@domain.com");

        ThreatIndicatorResponse response = new ThreatIndicatorResponse();
        response.setId(2L);
        response.setValue("scam@domain.com");

        when(threatIndicatorService.updateIndicator(eq(2L), any(ThreatIndicatorRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/fraud/indicator/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deactivateIndicator_shouldReturnIndicator() throws Exception {
        ThreatIndicatorResponse response = new ThreatIndicatorResponse();
        response.setId(1L);
        response.setActive(false);

        when(threatIndicatorService.deactivateIndicator(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/indicator/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void getIndicators_shouldReturnPage() throws Exception {
        Page<ThreatIndicatorResponse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(threatIndicatorService.searchIndicators(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/fraud/indicators?search=scam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
