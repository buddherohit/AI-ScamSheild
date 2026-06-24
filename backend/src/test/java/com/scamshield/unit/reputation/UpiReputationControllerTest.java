package com.scamshield.unit.reputation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.reputation.controller.UpiReputationController;
import com.scamshield.reputation.dto.UpiVerifyRequest;
import com.scamshield.reputation.dto.UpiVerifyResponse;
import com.scamshield.reputation.service.UpiReputationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UpiReputationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UpiReputationService upiReputationService;

    @InjectMocks
    private UpiReputationController upiReputationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(upiReputationController).build();
    }

    @Test
    void verifyUpi_shouldReturnReputationResponse() throws Exception {
        UpiVerifyResponse mockResponse = UpiVerifyResponse.builder()
                .upiId("test@upi")
                .score(80)
                .status("SAFE")
                .reasons(new ArrayList<>())
                .build();
        when(upiReputationService.verifyUpi(any(UpiVerifyRequest.class), anyString(), anyString())).thenReturn(mockResponse);

        UpiVerifyRequest request = new UpiVerifyRequest("test@upi");

        mockMvc.perform(post("/api/v1/reputation/verify-upi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(80))
                .andExpect(jsonPath("$.data.status").value("SAFE"));
    }

    @Test
    void getHistory_shouldReturnList() throws Exception {
        when(upiReputationService.getUserHistory()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reputation/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
