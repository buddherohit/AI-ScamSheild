package com.scamshield.unit.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.ai.dto.SmsAnalysisResponseDto;
import com.scamshield.ai.entity.*;
import com.scamshield.ai.provider.AIProvider;
import com.scamshield.ai.repository.*;
import com.scamshield.ai.service.AiExplanationEngine;
import com.scamshield.ai.service.FraudPatternEngine;
import com.scamshield.ai.service.PromptEngineeringService;
import com.scamshield.ai.service.SmsAnalysisService;
import com.scamshield.entity.Role;
import com.scamshield.entity.User;
import com.scamshield.exception.ValidationException;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.repository.UserRepository;
import com.scamshield.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsAnalysisServiceTest {

    @Mock private SmsAnalysisRepository smsAnalysisRepository;
    @Mock private SmsAnalysisReasonRepository smsAnalysisReasonRepository;
    @Mock private AiRequestRepository aiRequestRepository;
    @Mock private AiResponseRepository aiResponseRepository;
    @Mock private AnalysisHistoryRepository analysisHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ThreatIndicatorRepository threatIndicatorRepository;
    @Mock private AIProvider aiProvider;
    @Mock private AuditService auditService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @Mock private PromptEngineeringService promptEngineeringService;
    @Mock private FraudPatternEngine fraudPatternEngine;
    @Spy private AiExplanationEngine aiExplanationEngine;

    @InjectMocks
    private SmsAnalysisService smsAnalysisService;

    private User testUser;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@user.com")
                .name("Test User")
                .role(Role.ROLE_USER)
                .build();

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test@user.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void analyzeSms_withEmptyText_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> smsAnalysisService.analyzeSms("", "127.0.0.1", "agent"));
    }

    @Test
    void analyzeSms_withNormalText_shouldReturnLowRiskAnalysis() {
        String smsText = "Hey, let's meet at 5 PM.";
        
        when(fraudPatternEngine.evaluateText(anyString())).thenReturn(
                new FraudPatternEngine.PatternResult(List.of(), 0, "UNKNOWN")
        );
        when(promptEngineeringService.buildPrompt(eq("SMS_SYSTEM_PROMPT"), anyMap())).thenReturn("System prompt content");
        when(promptEngineeringService.buildPrompt(eq("SMS_USER_PROMPT"), anyMap())).thenReturn("User prompt content");
        
        when(aiProvider.getProviderName()).thenReturn("OpenAI");
        when(aiProvider.getModelName()).thenReturn("gpt-4o-mini");
        
        String aiResponseJson = "{\"riskScore\":5,\"riskLevel\":\"LOW\",\"category\":\"UNKNOWN\",\"summary\":\"Message is safe.\",\"recommendation\":\"No action required.\",\"indicators\":[]}";
        when(aiProvider.executePrompt(anyString(), anyString())).thenReturn(aiResponseJson);
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(testUser));

        SmsAnalysisResponseDto response = smsAnalysisService.analyzeSms(smsText, "127.0.0.1", "Mozilla");

        assertNotNull(response);
        assertEquals(5, response.getRiskScore());
        assertEquals("LOW", response.getRiskLevel());
        assertEquals("UNKNOWN", response.getCategory());
        assertEquals("Message is safe.", response.getSummary());

        verify(smsAnalysisRepository, times(1)).save(any(SmsAnalysis.class));
        verify(analysisHistoryRepository, times(1)).save(any(AnalysisHistory.class));
    }

    @Test
    void analyzeSms_withBlacklistedLink_shouldOverrideTo100Score() {
        String smsText = "URGENT: Click http://scam-link.com to unlock account.";
        
        when(fraudPatternEngine.evaluateText(anyString())).thenReturn(
                new FraudPatternEngine.PatternResult(List.of("URGENT"), 30, "BANKING_FRAUD")
        );
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("http://scam-link.com")).thenReturn(true);
        
        when(promptEngineeringService.buildPrompt(eq("SMS_SYSTEM_PROMPT"), anyMap())).thenReturn("System prompt content");
        when(promptEngineeringService.buildPrompt(eq("SMS_USER_PROMPT"), anyMap())).thenReturn("User prompt content");
        
        when(aiProvider.getProviderName()).thenReturn("OpenAI");
        when(aiProvider.getModelName()).thenReturn("gpt-4o-mini");
        
        String aiResponseJson = "{\"riskScore\":60,\"riskLevel\":\"MEDIUM\",\"category\":\"BANKING_FRAUD\",\"summary\":\"Suspicious account threat.\",\"recommendation\":\"Do not click.\",\"indicators\":[\"Urgency\"]}";
        when(aiProvider.executePrompt(anyString(), anyString())).thenReturn(aiResponseJson);
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(testUser));

        SmsAnalysisResponseDto response = smsAnalysisService.analyzeSms(smsText, "127.0.0.1", "Mozilla");

        assertNotNull(response);
        assertEquals(100, response.getRiskScore());
        assertEquals("CRITICAL", response.getRiskLevel());
        assertTrue(response.getIndicators().contains("Blacklisted Threat Link"));
    }

    @Test
    void getHistory_shouldReturnHistoryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        SmsAnalysis analysis = SmsAnalysis.builder()
                .id(1L)
                .smsText("Suspicious text")
                .riskScore(70)
                .riskLevel("HIGH")
                .category("OTP_SCAM")
                .summary("OTP Scam")
                .recommendation("Do not share OTP")
                .build();
        
        Page<SmsAnalysis> page = new PageImpl<>(List.of(analysis));
        
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(testUser));
        when(smsAnalysisRepository.findByUserId(testUser.getId(), pageable)).thenReturn(page);
        when(smsAnalysisReasonRepository.findByAnalysisId(1L)).thenReturn(List.of());

        Page<SmsAnalysisResponseDto> result = smsAnalysisService.getHistory(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Suspicious text", result.getContent().get(0).getSmsText());
    }
}
