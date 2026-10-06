package com.innspark.loginmonitor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "suspicious_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String username;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 50)
    private ActivityType activityType;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum ActivityType {
        BRUTE_FORCE,
        CREDENTIAL_STUFFING,
        UNUSUAL_SUCCESS,
        RATE_LIMIT_EXCEEDED
    }
}
