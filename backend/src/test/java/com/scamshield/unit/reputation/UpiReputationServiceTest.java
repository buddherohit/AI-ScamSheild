package com.scamshield.unit.reputation;

import com.scamshield.entity.User;
import com.scamshield.exception.BusinessException;
import com.scamshield.exception.ValidationException;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.ReportedEntityRepository;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.repository.UserRepository;
import com.scamshield.reputation.dto.UpiVerifyRequest;
import com.scamshield.reputation.dto.UpiVerifyResponse;
import com.scamshield.reputation.entity.UpiProfile;
import com.scamshield.reputation.repository.*;
import com.scamshield.reputation.service.UpiReputationService;
import com.scamshield.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpiReputationServiceTest {

    @Mock private UpiProfileRepository upiProfileRepository;
    @Mock private MerchantProfileRepository merchantProfileRepository;
    @Mock private VerificationRequestRepository verificationRequestRepository;
    @Mock private VerificationResultRepository verificationResultRepository;
    @Mock private RiskReasonRepository riskReasonRepository;
    @Mock private ReputationHistoryRepository reputationHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ThreatIndicatorRepository threatIndicatorRepository;
    @Mock private ReportedEntityRepository reportedEntityRepository;
    @Mock private FraudReportRepository fraudReportRepository;
    @Mock private AuditService auditService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private UpiReputationService upiReputationService;

    @BeforeEach
    void setUp() {
        upiReputationService = new UpiReputationService(
                upiProfileRepository,
                merchantProfileRepository,
                verificationRequestRepository,
                verificationResultRepository,
                riskReasonRepository,
                reputationHistoryRepository,
                userRepository,
                threatIndicatorRepository,
                reportedEntityRepository,
                fraudReportRepository,
                auditService
        );
        
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void verifyUpi_whenInvalidFormat_shouldThrowValidationException() {
        UpiVerifyRequest request = new UpiVerifyRequest("invalid_upi_id");
        assertThrows(ValidationException.class, () -> 
                upiReputationService.verifyUpi(request, "127.0.0.1", "Mozilla"));
    }

    @Test
    void verifyUpi_whenRateLimited_shouldThrowBusinessException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));
        when(verificationRequestRepository.countByIpAddressAndCreatedAtAfter(any(), any())).thenReturn(40L);

        UpiVerifyRequest request = new UpiVerifyRequest("test@upi");

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                upiReputationService.verifyUpi(request, "127.0.0.1", "Mozilla"));
    }

    @Test
    void verifyUpi_whenThreatIndicatorMatches_shouldReturnScoreZero() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        User user = new User();
        user.setId(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(verificationRequestRepository.countByIpAddressAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("test@upi")).thenReturn(true);
        when(upiProfileRepository.findByNormalizedUpi("test@upi")).thenReturn(Optional.empty());

        UpiVerifyRequest request = new UpiVerifyRequest("test@upi");

        // Act
        UpiVerifyResponse response = upiReputationService.verifyUpi(request, "127.0.0.1", "Mozilla");

        // Assert
        assertEquals(0, response.getScore());
        assertEquals("DANGEROUS", response.getStatus());
        assertTrue(response.getReasons().contains("Known Threat Indicator: Blacklisted by System"));
        verify(reputationHistoryRepository, times(1)).save(any());
        verify(verificationResultRepository, times(1)).save(any());
    }

    @Test
    void verifyUpi_whenCleanAndNoThreats_shouldReturnScore100() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        User user = new User();
        user.setId(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(verificationRequestRepository.countByIpAddressAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(threatIndicatorRepository.existsByValueAndIsActiveTrue("test@upi")).thenReturn(false);
        when(reportedEntityRepository.findByTypeAndValue("upi", "test@upi")).thenReturn(Optional.empty());
        when(upiProfileRepository.findByNormalizedUpi("test@upi")).thenReturn(Optional.empty());

        UpiVerifyRequest request = new UpiVerifyRequest("test@upi");

        // Act
        UpiVerifyResponse response = upiReputationService.verifyUpi(request, "127.0.0.1", "Mozilla");

        // Assert
        assertEquals(100, response.getScore());
        assertEquals("TRUSTED", response.getStatus());
        verify(reputationHistoryRepository, times(1)).save(any());
        verify(verificationResultRepository, times(1)).save(any());
    }
}
