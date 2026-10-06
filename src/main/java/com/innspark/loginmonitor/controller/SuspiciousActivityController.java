package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.dto.ApiResponse;
import com.innspark.loginmonitor.model.SuspiciousActivity;
import com.innspark.loginmonitor.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suspicious")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class SuspiciousActivityController {

    private final DetectionService detectionService;

    /**
     * GET /api/suspicious — Fetch all suspicious activity with reasoning
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SuspiciousActivity>>> getAllSuspiciousActivities(
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String username) {

        List<SuspiciousActivity> activities;

        if (ip != null) {
            activities = detectionService.getSuspiciousActivitiesByIp(ip);
        } else if (username != null) {
            activities = detectionService.getSuspiciousActivitiesByUsername(username);
        } else {
            activities = detectionService.getAllSuspiciousActivities();
        }

        return ResponseEntity.ok(ApiResponse.success("Suspicious activities fetched", activities));
    }

    /**
     * POST /api/suspicious/detect — Manually trigger detection (admin only)
     */
    @PostMapping("/detect")
    public ResponseEntity<ApiResponse<String>> triggerDetection() {
        detectionService.runDetection();
        return ResponseEntity.ok(ApiResponse.success("Detection cycle triggered successfully", "Done"));
    }
}
