package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.dto.LoginAttemptRequest;
import com.innspark.loginmonitor.model.LoginAttempt;
import com.innspark.loginmonitor.model.User;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import com.innspark.loginmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public LoginAttempt logLoginAttempt(LoginAttemptRequest request) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(request.getUsername().trim());
        attempt.setIpAddress(request.getIpAddress().trim());
        attempt.setStatus(request.getStatus());
        attempt.setUserAgent(request.getUserAgent());

        LoginAttempt saved = loginAttemptRepository.save(attempt);
        log.info("Logged login attempt: user={}, ip={}, status={}", 
                 saved.getUsername(), saved.getIpAddress(), saved.getStatus());

        // Update user failed attempts counter
        if (request.getStatus() == LoginAttempt.Status.FAILURE) {
            updateUserFailedAttempts(request.getUsername(), true);
        } else {
            updateUserFailedAttempts(request.getUsername(), false);
        }

        return saved;
    }

    private void updateUserFailedAttempts(String username, boolean increment) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(user -> {
            if (increment) {
                user.setFailedAttempts(user.getFailedAttempts() + 1);
                // Require CAPTCHA after 3 failures
                if (user.getFailedAttempts() >= 3) {
                    user.setCaptchaRequired(true);
                }
            } else {
                user.setFailedAttempts(0);
                user.setCaptchaRequired(false);
            }
            userRepository.save(user);
        });
    }

    public List<LoginAttempt> getAllAttempts() {
        return loginAttemptRepository.findAll();
    }

    public List<LoginAttempt> getAttemptsByUsername(String username) {
        return loginAttemptRepository.findByUsername(username);
    }

    public List<LoginAttempt> getAttemptsByIp(String ipAddress) {
        return loginAttemptRepository.findByIpAddress(ipAddress);
    }

    public List<LoginAttempt> getAttemptsByStatus(LoginAttempt.Status status) {
        return loginAttemptRepository.findByStatus(status);
    }

    public List<LoginAttempt> getAttemptsByUsernameAndStatus(String username, LoginAttempt.Status status) {
        return loginAttemptRepository.findByUsernameAndStatus(username, status);
    }

    public List<LoginAttempt> getAttemptsByIpAndStatus(String ip, LoginAttempt.Status status) {
        return loginAttemptRepository.findByIpAddressAndStatus(ip, status);
    }
}
