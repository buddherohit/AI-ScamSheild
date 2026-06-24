package com.scamshield.unit.qr;

import com.scamshield.entity.User;
import com.scamshield.exception.BusinessException;
import com.scamshield.repository.UserRepository;
import com.scamshield.reputation.dto.UpiVerifyResponse;
import com.scamshield.reputation.service.UpiReputationService;
import com.scamshield.qr.dto.QrScanResponse;
import com.scamshield.qr.repository.QrScanRepository;
import com.scamshield.qr.service.*;
import com.scamshield.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrIntelligenceServiceTest {

    @Mock private QrScanRepository qrScanRepository;
    @Mock private UserRepository userRepository;
    @Mock private QRParser qrParser;
    @Mock private UPIQRParser upiQrParser;
    @Mock private MerchantQRParser merchantQrParser;
    @Mock private UpiReputationService upiReputationService;
    @Mock private AuditService auditService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private QrIntelligenceService qrIntelligenceService;

    @BeforeEach
    void setUp() {
        qrIntelligenceService = new QrIntelligenceService(
                qrScanRepository,
                userRepository,
                qrParser,
                upiQrParser,
                merchantQrParser,
                upiReputationService,
                auditService
        );

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void scanAndVerifyImage_whenInvalidImage_shouldThrowBusinessException() {
        // Arrange
        byte[] bytes = new byte[]{1, 2, 3};
        when(qrParser.parse(bytes)).thenThrow(new QrParsingException("Invalid or corrupt image"));

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                qrIntelligenceService.scanAndVerifyImage(bytes, "127.0.0.1", "Mozilla"));
    }

    @Test
    void verifyRawText_whenNoUpiFound_shouldThrowBusinessException() {
        // Arrange
        String text = "upi://pay?pn=MerchantName"; // missing pa parameter
        when(upiQrParser.extractUpiId(text)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                qrIntelligenceService.verifyRawText(text, "127.0.0.1", "Mozilla"));
    }

    @Test
    void verifyRawText_whenValidUpiFound_shouldAssessRiskCorrectly() {
        // Arrange
        String text = "upi://pay?pa=test@upi&pn=MerchantName";
        when(upiQrParser.extractUpiId(text)).thenReturn("test@upi");
        when(merchantQrParser.extractMerchantName(text)).thenReturn("MerchantName");

        UpiVerifyResponse mockRepResponse = UpiVerifyResponse.builder()
                .upiId("test@upi")
                .score(80) // 80 reputation score -> risk score = 20 (LOW risk)
                .status("SAFE")
                .reasons(new ArrayList<>())
                .build();
        when(upiReputationService.verifyUpi(any(), any(), any())).thenReturn(mockRepResponse);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));

        // Act
        QrScanResponse response = qrIntelligenceService.verifyRawText(text, "127.0.0.1", "Mozilla");

        // Assert
        assertEquals("test@upi", response.getUpi());
        assertEquals("MerchantName", response.getMerchant());
        assertEquals(20, response.getRiskScore());
        assertEquals("LOW", response.getRiskLevel());
        assertEquals("Safe to proceed", response.getRecommendation());
        verify(qrScanRepository, times(1)).save(any());
    }
}
