package com.scamshield.unit.qr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.qr.controller.QrController;
import com.scamshield.qr.dto.QrScanResponse;
import com.scamshield.qr.dto.QrVerifyRequest;
import com.scamshield.qr.service.QrIntelligenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QrControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QrIntelligenceService qrIntelligenceService;

    @InjectMocks
    private QrController qrController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(qrController).build();
    }

    @Test
    void verifyQrText_shouldReturnScanResponse() throws Exception {
        QrScanResponse mockResponse = QrScanResponse.builder()
                .upi("test@upi")
                .merchant("Test Merchant")
                .riskScore(20)
                .riskLevel("LOW")
                .recommendation("Safe to proceed")
                .build();
        when(qrIntelligenceService.verifyRawText(anyString(), anyString(), anyString())).thenReturn(mockResponse);

        QrVerifyRequest request = new QrVerifyRequest("upi://pay?pa=test@upi&pn=Test%20Merchant");

        mockMvc.perform(post("/api/v1/qr/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.upi").value("test@upi"))
                .andExpect(jsonPath("$.data.merchant").value("Test Merchant"))
                .andExpect(jsonPath("$.data.riskScore").value(20));
    }

    @Test
    void getHistory_shouldReturnList() throws Exception {
        when(qrIntelligenceService.getScanHistory()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/qr/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
