package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.dto.DashboardStats;
import com.innspark.loginmonitor.model.LoginAttempt;
import com.innspark.loginmonitor.model.SuspiciousActivity;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import com.innspark.loginmonitor.repository.SuspiciousActivityRepository;
import com.innspark.loginmonitor.repository.UserRepository;
import com.innspark.loginmonitor.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final SuspiciousActivityRepository suspiciousActivityRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;

    public DashboardStats getStats() {
        long total = loginAttemptRepository.count();
        long success = loginAttemptRepository.countByStatus(LoginAttempt.Status.SUCCESS);
        long failed = loginAttemptRepository.countByStatus(LoginAttempt.Status.FAILURE);
        long totalSuspicious = suspiciousActivityRepository.count();
        long bruteForce = suspiciousActivityRepository.countByActivityType(SuspiciousActivity.ActivityType.BRUTE_FORCE);
        long unusualSuccess = suspiciousActivityRepository.countByActivityType(SuspiciousActivity.ActivityType.UNUSUAL_SUCCESS);
        long activeSessions = sessionRepository.findByExpiredFalse().size();
        long totalUsers = userRepository.count();

        return new DashboardStats(total, success, failed, totalSuspicious,
                                  bruteForce, unusualSuccess, activeSessions, totalUsers);
    }
}
