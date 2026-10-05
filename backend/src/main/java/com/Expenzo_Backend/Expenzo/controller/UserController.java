package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.dto.*;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add-user")
    public UserResponseDTO registerUser(@RequestBody UserRequestDTO requestDTO) {
        return userService.registerUser(requestDTO);
    }

    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    // ─── OTP Verify (public — user not logged in yet) ───────
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        userService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
    }

    // ─── Resend OTP (public) ────────────────────────────────
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        userService.resendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "New OTP sent to your email."));
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/all-users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/update")
    public UserResponseDTO updateUser(@RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(updateUserRequest);
    }

    @PutMapping("/change-password")
    public UserResponseDTO changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return userService.changePassword(changePasswordRequest);
    }

    @PutMapping("/change-profile-image/{id}")
    public User changeProfileImage(@PathVariable Long id, @RequestParam String image) {
        return userService.changeProfileImage(id, image);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/me")
    public UserResponseDTO me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
