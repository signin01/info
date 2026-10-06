package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.model.User;
import com.innspark.loginmonitor.model.UserSession;
import com.innspark.loginmonitor.repository.UserRepository;
import com.innspark.loginmonitor.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Value("${app.session.expiry-minutes:15}")
    private int sessionExpiryMinutes;

    /**
     * Create a new session for a user after successful login
     */
    @Transactional
    public UserSession createSession(String username, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionId(UUID.randomUUID().toString());
        session.setIpAddress(ipAddress);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActive(LocalDateTime.now());
        session.setExpired(false);

        UserSession saved = sessionRepository.save(session);
        log.info("Session created for user: {}, sessionId: {}", username, saved.getSessionId());
        return saved;
    }

    /**
     * Update last active timestamp for a session
     */
    @Transactional
    public void touchSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setLastActive(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    /**
     * Expire a session manually
     */
    @Transactional
    public void expireSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setExpired(true);
            sessionRepository.save(session);
            log.info("Session expired: {}", sessionId);
        });
    }

    /**
     * Expire all inactive sessions (called by scheduler)
     */
    @Transactional
    public int expireInactiveSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(sessionExpiryMinutes);
        int count = sessionRepository.expireOldSessions(cutoff);
        if (count > 0) {
            log.info("Expired {} inactive sessions", count);
        }
        return count;
    }

    /**
     * Get all active sessions for a user
     */
    public List<UserSession> getActiveSessionsByUser(Long userId) {
        return sessionRepository.findByUserIdAndExpiredFalse(userId);
    }

    /**
     * Get all active sessions (admin view)
     */
    public List<UserSession> getAllActiveSessions() {
        return sessionRepository.findByExpiredFalse();
    }

    /**
     * Validate if a session is still active
     */
    public boolean isSessionValid(String sessionId) {
        Optional<UserSession> session = sessionRepository.findBySessionId(sessionId);
        if (session.isEmpty() || session.get().isExpired()) return false;

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(sessionExpiryMinutes);
        return session.get().getLastActive().isAfter(cutoff);
    }
}
