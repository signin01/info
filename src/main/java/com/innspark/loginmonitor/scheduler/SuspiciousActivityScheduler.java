package com.innspark.loginmonitor.scheduler;

import com.innspark.loginmonitor.service.DetectionService;
import com.innspark.loginmonitor.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuspiciousActivityScheduler {

    private final DetectionService detectionService;
    private final SessionService sessionService;

    /**
     * Run suspicious activity detection every 2 minutes
     */
    @Scheduled(fixedDelay = 120000) // 2 minutes
    public void detectSuspiciousActivity() {
        log.debug("Scheduler: Running detection cycle");
        detectionService.runDetection();
    }

    /**
     * Expire inactive sessions every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void expireInactiveSessions() {
        log.debug("Scheduler: Checking for expired sessions");
        int expired = sessionService.expireInactiveSessions();
        if (expired > 0) {
            log.info("Scheduler: Expired {} inactive sessions", expired);
        }
    }
}
