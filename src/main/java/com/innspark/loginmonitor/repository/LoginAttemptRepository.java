package com.innspark.loginmonitor.repository;

import com.innspark.loginmonitor.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    List<LoginAttempt> findByUsername(String username);

    List<LoginAttempt> findByIpAddress(String ipAddress);

    List<LoginAttempt> findByStatus(LoginAttempt.Status status);

    List<LoginAttempt> findByUsernameAndStatus(String username, LoginAttempt.Status status);

    List<LoginAttempt> findByIpAddressAndStatus(String ipAddress, LoginAttempt.Status status);

    @Query("SELECT l FROM LoginAttempt l WHERE l.ipAddress = :ip AND l.status = 'FAILURE' AND l.timestamp >= :since")
    List<LoginAttempt> findRecentFailuresByIp(@Param("ip") String ip, @Param("since") LocalDateTime since);

    @Query("SELECT l FROM LoginAttempt l WHERE l.username = :username AND l.status = 'FAILURE' AND l.timestamp >= :since")
    List<LoginAttempt> findRecentFailuresByUsername(@Param("username") String username, @Param("since") LocalDateTime since);

    @Query("SELECT l FROM LoginAttempt l WHERE l.timestamp >= :since ORDER BY l.timestamp DESC")
    List<LoginAttempt> findRecentAttempts(@Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT l.ipAddress FROM LoginAttempt l WHERE l.timestamp >= :since")
    List<String> findDistinctIpsSince(@Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT l.username FROM LoginAttempt l WHERE l.timestamp >= :since")
    List<String> findDistinctUsernamesSince(@Param("since") LocalDateTime since);

    long countByStatus(LoginAttempt.Status status);

    long countByUsername(String username);

    long countByUsernameAndStatus(String username, LoginAttempt.Status status);
}
