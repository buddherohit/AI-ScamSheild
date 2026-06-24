package com.scamshield.unit.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.ai.controller.AiController;
import com.scamshield.ai.dto.SmsAnalysisRequestDto;
import com.scamshield.ai.dto.SmsAnalysisResponseDto;
import com.scamshield.ai.service.SmsAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SmsAnalysisService smsAnalysisService;

    @InjectMocks
    private AiController aiController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    @Test
    void analyzeSms_shouldReturnResponse() throws Exception {
        SmsAnalysisResponseDto responseDto = SmsAnalysisResponseDto.builder()
                .id(1L)
                .smsText("Urgent OTP request")
                .riskScore(80)
                .riskLevel("HIGH")
                .category("OTP_SCAM")
                .summary("Scam detected")
                .recommendation("Do not share OTP")
                .indicators(List.of("OTP", "Urgency"))
                .build();

        when(smsAnalysisService.analyzeSms(anyString(), anyString(), anyString())).thenReturn(responseDto);

        SmsAnalysisRequestDto request = SmsAnalysisRequestDto.builder()
                .smsText("Urgent OTP request")
                .build();

        mockMvc.perform(post("/api/v1/ai/analyze-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskScore").value(80))
                .andExpect(jsonPath("$.data.category").value("OTP_SCAM"));
    }

    @Test
    void getHistory_shouldReturnPage() throws Exception {
        when(smsAnalysisService.getHistory(any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/v1/ai/history")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getAnalysisDetails_shouldReturnDetails() throws Exception {
        SmsAnalysisResponseDto responseDto = SmsAnalysisResponseDto.builder()
                .id(1L)
                .smsText("Some sms text")
                .riskScore(20)
                .riskLevel("LOW")
                .category("UNKNOWN")
                .build();

        when(smsAnalysisService.getAnalysisDetails(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/ai/analysis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.riskScore").value(20));
    }

    @Test
    void deleteAnalysis_shouldReturnSuccess() throws Exception {
        doNothing().when(smsAnalysisService).deleteAnalysis(1L);

        mockMvc.perform(delete("/api/v1/ai/analysis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Analysis record deleted successfully"));
    }

    @Test
    void getFraudCategories_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/ai/fraud-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("BANKING_FRAUD"));
    }
}
