package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Sends a password reset email with a unique token.
     * Silently skips Google users and unknown emails for security.
     */
    public void sendResetEmail(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    // ✅ Google users have no password to reset — silently skip
                    if ("GOOGLE".equals(user.getProvider())) {
                        System.out.println("⚠️ Password reset requested for GOOGLE user (skipped): " + email);
                        return;
                    }

                    String token = UUID.randomUUID().toString();
                    user.setResetToken(token);
                    user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
                    userRepository.save(user);

                    // ✅ Build full reset link using configurable frontend URL
                    String resetLink = frontendUrl + "/reset-password?token=" + token;

                    System.out.println("🔗 Password reset link for " + email + ": " + resetLink);

                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            user.getName(),
                            resetLink
                    );
                },
                () -> System.out.println("⚠️ Password reset requested for unknown email: " + email)
        );
    }

    /**
     * Validates that a reset token exists and hasn't expired.
     */
    public void verifyToken(String token) {
        loadValidUserByToken(token); // throws if invalid/expired
    }

    /**
     * Resets the user's password using the token.
     */
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        User user = loadValidUserByToken(token);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);

        System.out.println("✅ Password reset successful for: " + user.getEmail());
    }

    // ─────────────────────────────────────────────────────────
    // Private helper — shared by verifyToken() and resetPassword()
    // ─────────────────────────────────────────────────────────
    private User loadValidUserByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Reset token is missing");
        }

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (user.getResetTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new RuntimeException("Reset link has expired. Please request a new one.");
        }

        return user;
    }
}