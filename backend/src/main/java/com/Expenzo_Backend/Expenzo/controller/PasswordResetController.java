package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.dto.ForgotPasswordRequest;
import com.Expenzo_Backend.Expenzo.dto.ResetPasswordRequest;
import com.Expenzo_Backend.Expenzo.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetEmail(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, a reset link has been sent."
        ));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        passwordResetService.verifyToken(token);
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

//    @GetMapping("/bcrypt/{raw}")
//    public String generateBcrypt(@PathVariable String raw) {
//        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(raw);
//    }
}