package com.innspark.loginmonitor.repository;

import com.innspark.loginmonitor.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    List<UserSession> findByUserIdAndExpiredFalse(Long userId);

    List<UserSession> findByExpiredFalse();

    @Query("SELECT s FROM UserSession s WHERE s.expired = false AND s.lastActive < :cutoff")
    List<UserSession> findExpiredSessions(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.expired = true WHERE s.lastActive < :cutoff AND s.expired = false")
    int expireOldSessions(@Param("cutoff") LocalDateTime cutoff);
}
