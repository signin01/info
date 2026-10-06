package com.innspark.loginmonitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email:admin@innspark.com}")
    private String adminEmail;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    /**
     * Send brute force alert to admin
     */
    @Async
    public void notifyAdminBruteForce(String ipAddress, int failureCount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(adminEmail);
            message.setSubject("[ALERT] Brute Force Detected - Login Activity Monitor");
            message.setText(
                "SUSPICIOUS ACTIVITY DETECTED\n\n" +
                "Type: Brute Force Attack\n" +
                "IP Address: " + ipAddress + "\n" +
                "Failed Attempts: " + failureCount + "\n\n" +
                "Please investigate immediately.\n\n" +
                "-- InnSpark Login Activity Monitor"
            );
            mailSender.send(message);
            log.info("Brute force alert sent to admin for IP: {}", ipAddress);
        } catch (Exception e) {
            log.warn("Failed to send brute force email alert: {}", e.getMessage());
        }
    }

    /**
     * Notify a user about suspicious activity on their account
     */
    @Async
    public void notifyUserSuspiciousActivity(String username, String reason) {
        try {
            // In real scenario, fetch user email from DB
            // For now, log the notification
            log.info("User notification triggered for {}: {}", username, reason);

            // Uncomment when user email is available:
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom(senderEmail);
            // message.setTo(userEmail);
            // message.setSubject("[SECURITY ALERT] Suspicious login activity on your account");
            // message.setText("Dear " + username + ",\n\n" +
            //     "We detected suspicious activity on your account:\n" +
            //     reason + "\n\n" +
            //     "If this was not you, please change your password immediately.\n\n" +
            //     "-- InnSpark Security Team");
            // mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send user notification: {}", e.getMessage());
        }
    }
}
