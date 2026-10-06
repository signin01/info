package com.innspark.loginmonitor.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    @Value("${app.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.rate-limit.window-minutes:1}")
    private int windowMinutes;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Get or create a rate limit bucket for a given IP address
     */
    public Bucket resolveBucket(String ipAddress) {
        return buckets.computeIfAbsent(ipAddress, this::createNewBucket);
    }

    private Bucket createNewBucket(String ipAddress) {
        Bandwidth limit = Bandwidth.classic(
            maxAttempts,
            Refill.greedy(maxAttempts, Duration.ofMinutes(windowMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Check if request from IP is allowed (not rate limited)
     * Returns true if allowed, false if rate limited
     */
    public boolean tryConsume(String ipAddress) {
        Bucket bucket = resolveBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Get remaining tokens for an IP
     */
    public long getRemainingTokens(String ipAddress) {
        return resolveBucket(ipAddress).getAvailableTokens();
    }
}
