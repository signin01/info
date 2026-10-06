package com.innspark.loginmonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalLoginAttempts;
    private long successfulLogins;
    private long failedLogins;
    private long totalSuspiciousActivities;
    private long bruteForceCount;
    private long unusualSuccessCount;
    private long activeSessions;
    private long totalUsers;
}
