package com.innspark.loginmonitor.dto;

import com.innspark.loginmonitor.model.LoginAttempt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginAttemptRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_@.]{3,100}$", message = "Invalid username format")
    private String username;

    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
             message = "Invalid IP address format")
    private String ipAddress;

    private LoginAttempt.Status status;

    private String userAgent;

    // Simulated password field (BCrypt hashed in real scenario)
    private String password;

    // Optional CAPTCHA token
    private String captchaToken;

    // Optional OTP code for 2FA
    private String otpCode;
}
