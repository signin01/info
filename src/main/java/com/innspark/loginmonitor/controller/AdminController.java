package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.dto.ApiResponse;
import com.innspark.loginmonitor.dto.DashboardStats;
import com.innspark.loginmonitor.model.UserSession;
import com.innspark.loginmonitor.service.DashboardService;
import com.innspark.loginmonitor.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final SessionService sessionService;

    /**
     * GET /api/dashboard/stats — Get login and suspicious activity statistics
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", stats));
    }

    /**
     * GET /api/sessions — View all active sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<UserSession>>> getActiveSessions() {
        List<UserSession> sessions = sessionService.getAllActiveSessions();
        return ResponseEntity.ok(ApiResponse.success("Active sessions fetched", sessions));
    }

    /**
     * DELETE /api/sessions/{sessionId} — Expire a specific session
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<String>> expireSession(@PathVariable String sessionId) {
        sessionService.expireSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session expired successfully", sessionId));
    }
}
