package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.model.LoginAttempt;
import com.innspark.loginmonitor.model.SuspiciousActivity;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import com.innspark.loginmonitor.repository.SuspiciousActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final SuspiciousActivityRepository suspiciousActivityRepository;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.detection.brute-force-threshold:5}")
    private int bruteForceThreshold;

    @Value("${app.detection.window-minutes:10}")
    private int windowMinutes;

    @Value("${app.detection.unusual-success-threshold:3}")
    private int unusualSuccessThreshold;

    /**
     * Main detection logic — called by scheduler every 2 minutes
     */
    @Transactional
    public void runDetection() {
        log.info("Running suspicious activity detection...");
        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);

        detectBruteForceByIp(since);
        detectBruteForceByUsername(since);
        detectUnusualSuccess(since);

        log.info("Detection cycle complete.");
    }

    /**
     * Detects multiple failed logins from the same IP in the time window
     */
    private void detectBruteForceByIp(LocalDateTime since) {
        List<String> recentIps = loginAttemptRepository.findDistinctIpsSince(since);

        for (String ip : recentIps) {
            List<LoginAttempt> failures = loginAttemptRepository.findRecentFailuresByIp(ip, since);

            if (failures.size() >= bruteForceThreshold) {
                // Check for duplicate entry in last window
                List<SuspiciousActivity> existing = suspiciousActivityRepository
                        .findRecentByIpAndType(ip, SuspiciousActivity.ActivityType.BRUTE_FORCE, since);

                if (existing.isEmpty()) {
                    String reason = failures.size() + " failed logins from IP " + ip
                            + " in " + windowMinutes + " minutes";
                    logSuspiciousActivity(ip, null, reason, SuspiciousActivity.ActivityType.BRUTE_FORCE);
                    log.warn("BRUTE FORCE detected from IP: {}", ip);
                    emailNotificationService.notifyAdminBruteForce(ip, failures.size());
                }
            }
        }
    }

    /**
     * Detects multiple failed logins for the same username (credential stuffing)
     */
    private void detectBruteForceByUsername(LocalDateTime since) {
        List<String> recentUsernames = loginAttemptRepository.findDistinctUsernamesSince(since);

        for (String username : recentUsernames) {
            List<LoginAttempt> failures = loginAttemptRepository.findRecentFailuresByUsername(username, since);

            if (failures.size() >= bruteForceThreshold) {
                List<SuspiciousActivity> existing = suspiciousActivityRepository
                        .findRecentByUsernameAndType(username, SuspiciousActivity.ActivityType.CREDENTIAL_STUFFING, since);

                if (existing.isEmpty()) {
                    String reason = failures.size() + " failed logins for username '" + username
                            + "' in " + windowMinutes + " minutes";
                    String ip = failures.get(0).getIpAddress();
                    logSuspiciousActivity(ip, username, reason, SuspiciousActivity.ActivityType.CREDENTIAL_STUFFING);
                    log.warn("CREDENTIAL STUFFING detected for user: {}", username);
                    emailNotificationService.notifyUserSuspiciousActivity(username, reason);
                }
            }
        }
    }

    /**
     * Detects a successful login immediately following multiple failures
     */
    private void detectUnusualSuccess(LocalDateTime since) {
        List<String> recentUsernames = loginAttemptRepository.findDistinctUsernamesSince(since);

        for (String username : recentUsernames) {
            List<LoginAttempt> recentAttempts = loginAttemptRepository.findRecentAttempts(since)
                    .stream()
                    .filter(a -> a.getUsername().equals(username))
                    .toList();

            if (recentAttempts.size() < unusualSuccessThreshold + 1) continue;

            // Check if last attempt is SUCCESS and previous N are FAILURES
            LoginAttempt latest = recentAttempts.get(0);
            if (latest.getStatus() == LoginAttempt.Status.SUCCESS) {
                long recentFailures = recentAttempts.stream()
                        .skip(1)
                        .limit(unusualSuccessThreshold)
                        .filter(a -> a.getStatus() == LoginAttempt.Status.FAILURE)
                        .count();

                if (recentFailures >= unusualSuccessThreshold) {
                    List<SuspiciousActivity> existing = suspiciousActivityRepository
                            .findRecentByUsernameAndType(username, SuspiciousActivity.ActivityType.UNUSUAL_SUCCESS, since);

                    if (existing.isEmpty()) {
                        String reason = "Successful login for '" + username
                                + "' after " + recentFailures + " consecutive failures";
                        logSuspiciousActivity(latest.getIpAddress(), username, reason,
                                SuspiciousActivity.ActivityType.UNUSUAL_SUCCESS);
                        log.warn("UNUSUAL SUCCESS detected for user: {}", username);
                        emailNotificationService.notifyUserSuspiciousActivity(username, reason);
                    }
                }
            }
        }
    }

    @Transactional
    public SuspiciousActivity logSuspiciousActivity(String ip, String username,
                                                     String reason, SuspiciousActivity.ActivityType type) {
        SuspiciousActivity activity = new SuspiciousActivity();
        activity.setIpAddress(ip);
        activity.setUsername(username);
        activity.setReason(reason);
        activity.setActivityType(type);
        return suspiciousActivityRepository.save(activity);
    }

    public List<SuspiciousActivity> getAllSuspiciousActivities() {
        return suspiciousActivityRepository.findAllOrderByTimestampDesc();
    }

    public List<SuspiciousActivity> getSuspiciousActivitiesByIp(String ip) {
        return suspiciousActivityRepository.findByIpAddress(ip);
    }

    public List<SuspiciousActivity> getSuspiciousActivitiesByUsername(String username) {
        return suspiciousActivityRepository.findByUsername(username);
    }
}
