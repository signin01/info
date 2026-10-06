package com.innspark.loginmonitor.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.innspark.loginmonitor.model.User;
import com.innspark.loginmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final UserRepository userRepository;

    /**
     * Generate a new TOTP secret for a user
     */
    @Transactional
    public String generateSecret(String username) {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOtpSecret(secret);
            user.setOtpEnabled(true);
            userRepository.save(user);
        });

        log.info("2FA secret generated for user: {}", username);
        return secret;
    }

    /**
     * Generate QR code URL for Google Authenticator app
     */
    public String generateQrUrl(String username, String secret) {
        return "otpauth://totp/LoginMonitor:" + username
                + "?secret=" + secret
                + "&issuer=InnSparkLoginMonitor";
    }

    /**
     * Verify OTP code entered by user
     */
    public boolean verifyOtp(String username, int otpCode) {
        return userRepository.findByUsername(username)
                .filter(User::isOtpEnabled)
                .map(user -> {
                    boolean valid = googleAuthenticator.authorize(user.getOtpSecret(), otpCode);
                    log.info("OTP verification for user {}: {}", username, valid ? "SUCCESS" : "FAILED");
                    return valid;
                })
                .orElse(false);
    }

    /**
     * Check if user has 2FA enabled
     */
    public boolean isOtpEnabled(String username) {
        return userRepository.findByUsername(username)
                .map(User::isOtpEnabled)
                .orElse(false);
    }

    /**
     * Disable 2FA for a user
     */
    @Transactional
    public void disableOtp(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOtpEnabled(false);
            user.setOtpSecret(null);
            userRepository.save(user);
            log.info("2FA disabled for user: {}", username);
        });
    }
}
