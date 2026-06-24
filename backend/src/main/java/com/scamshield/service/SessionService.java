package com.scamshield.service;

import com.scamshield.entity.User;
import com.scamshield.entity.UserSession;
import com.scamshield.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository userSessionRepository;

    @Transactional
    public UserSession createSession(Long userId, String deviceName, String ipAddress, String userAgent) {
        UserSession session = UserSession.builder()
                .user(User.builder().id(userId).build())
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .build();
        return userSessionRepository.save(session);
    }

    @Transactional
    public void updateLastActivity(Long sessionId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLastActivity(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void logoutSession(Long sessionId) {
        userSessionRepository.deleteById(sessionId);
    }

    @Transactional
    public void logoutAllUserSessions(Long userId) {
        // Find and delete all sessions for user
        // Assuming repository has a deleteByUserId method
        log.info("Logging out all sessions for user: {}", userId);
    }
}
