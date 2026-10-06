package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.config.RateLimitConfig;
import com.innspark.loginmonitor.dto.ApiResponse;
import com.innspark.loginmonitor.dto.LoginAttemptRequest;
import com.innspark.loginmonitor.model.LoginAttempt;
import com.innspark.loginmonitor.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final LoginService loginService;
    private final RateLimitConfig rateLimitConfig;

    /**
     * POST /api/login — Log a login attempt
     * Public endpoint (simulates actual login flow)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LoginAttempt>> logLoginAttempt(
            @Valid @RequestBody LoginAttemptRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);

        // Override IP from request body if provided (for simulation), else use actual IP
        if (request.getIpAddress() == null || request.getIpAddress().isBlank()) {
            request.setIpAddress(clientIp);
        }

        // Rate limiting check
        if (!rateLimitConfig.tryConsume(request.getIpAddress())) {
            long remaining = rateLimitConfig.getRemainingTokens(request.getIpAddress());
            log.warn("Rate limit exceeded for IP: {}", request.getIpAddress());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many login attempts. Please try again later. " +
                                            "Remaining tokens: " + remaining));
        }

        LoginAttempt saved = loginService.logLoginAttempt(request);
        return ResponseEntity.ok(ApiResponse.success("Login attempt logged successfully", saved));
    }

    /**
     * GET /api/login — Fetch all login attempts with optional filters
     * Admin only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<LoginAttempt>>> getLoginAttempts(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String status) {

        List<LoginAttempt> attempts;

        if (username != null && status != null) {
            LoginAttempt.Status s = LoginAttempt.Status.valueOf(status.toUpperCase());
            attempts = loginService.getAttemptsByUsernameAndStatus(username, s);
        } else if (ip != null && status != null) {
            LoginAttempt.Status s = LoginAttempt.Status.valueOf(status.toUpperCase());
            attempts = loginService.getAttemptsByIpAndStatus(ip, s);
        } else if (username != null) {
            attempts = loginService.getAttemptsByUsername(username);
        } else if (ip != null) {
            attempts = loginService.getAttemptsByIp(ip);
        } else if (status != null) {
            LoginAttempt.Status s = LoginAttempt.Status.valueOf(status.toUpperCase());
            attempts = loginService.getAttemptsByStatus(s);
        } else {
            attempts = loginService.getAllAttempts();
        }

        return ResponseEntity.ok(ApiResponse.success("Login attempts fetched", attempts));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
