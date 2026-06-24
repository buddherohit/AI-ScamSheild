package com.scamshield.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.controller.DashboardController;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.RiskAssessmentRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FraudReportRepository fraudReportRepository;

    @Mock
    private ThreatIndicatorRepository threatIndicatorRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void getDashboardSummary_shouldReturnMetrics() throws Exception {
        when(fraudReportRepository.count()).thenReturn(100L);
        when(threatIndicatorRepository.count()).thenReturn(50L);
        when(riskAssessmentRepository.countByRiskLevel("CRITICAL")).thenReturn(2L);
        when(riskAssessmentRepository.countByRiskLevel("HIGH")).thenReturn(3L);
        when(fraudReportRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metrics.totalThreatsBlocked").value(150))
                .andExpect(jsonPath("$.data.metrics.activeAlerts").value(5));
    }
}
