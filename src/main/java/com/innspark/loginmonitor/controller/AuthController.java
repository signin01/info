package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.dto.*;
import com.innspark.loginmonitor.model.User;
import com.innspark.loginmonitor.security.JwtUtil;
import com.innspark.loginmonitor.service.TwoFactorService;
import com.innspark.loginmonitor.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final TwoFactorService twoFactorService;

    /**
     * POST /api/auth/register — Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username already exists"));
        }
        userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", request.getUsername()));
    }

    /**
     * POST /api/auth/login — Authenticate and get JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody RegisterRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userService.findByUsername(request.getUsername());

            // Check if 2FA is enabled
            if (user.isOtpEnabled()) {
                AuthResponse response = new AuthResponse(null, user.getUsername(),
                        user.getRole().name(), true, "OTP required. Please verify with your authenticator app.");
                return ResponseEntity.ok(ApiResponse.success("OTP required", response));
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            AuthResponse response = new AuthResponse(token, user.getUsername(),
                    user.getRole().name(), false, "Login successful");

            return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));

        } catch (Exception e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }

    /**
     * POST /api/auth/verify-otp — Verify 2FA OTP and get JWT token
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @RequestParam String username,
            @RequestParam int otpCode) {

        if (!twoFactorService.verifyOtp(username, otpCode)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid OTP code"));
        }

        User user = userService.findByUsername(username);
        String token = jwtUtil.generateToken(username, user.getRole().name());
        AuthResponse response = new AuthResponse(token, username, user.getRole().name(), false, "2FA verified successfully");

        return ResponseEntity.ok(ApiResponse.success("OTP verified", response));
    }

    /**
     * POST /api/auth/setup-2fa — Generate 2FA secret for user
     */
    @PostMapping("/setup-2fa")
    public ResponseEntity<ApiResponse<String>> setup2fa(@RequestParam String username) {
        String secret = twoFactorService.generateSecret(username);
        String qrUrl = twoFactorService.generateQrUrl(username, secret);
        return ResponseEntity.ok(ApiResponse.success("Scan this QR URL with Google Authenticator", qrUrl));
    }
}
