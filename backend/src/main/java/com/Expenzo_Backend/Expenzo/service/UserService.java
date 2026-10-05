package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.dto.*;
import com.Expenzo_Backend.Expenzo.exception.UserNotFoundException;
import com.Expenzo_Backend.Expenzo.exception.WrongPasswordException;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import com.Expenzo_Backend.Expenzo.security.CustomUserDetailsService;
import com.Expenzo_Backend.Expenzo.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    // ───────────────────────────── REGISTER ─────────────────────────────
    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setName(requestDTO.getName());
        user.setUsername(requestDTO.getUsername());
        user.setEmail(requestDTO.getEmail());
        user.setPhoneNumber(requestDTO.getPhoneNumber());
        user.setImage(requestDTO.getImage());

        String tempPassword = generateSecurePassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setProvider("LOCAL");

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getName(), tempPassword);
        emailService.sendOtpEmail(user.getEmail(), user.getName(), otp);

        return mapToResponseDTO(savedUser);
    }

    // ───────────────────────────── LOGIN ─────────────────────────────
    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if ("GOOGLE".equals(user.getProvider())) {
            throw new RuntimeException(
                    "This account uses Google sign-in. Please use 'Continue with Google' below."
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setImage(user.getImage());
        response.setCreatedAt(user.getCreatedAt());
        response.setMustChangePassword(user.isMustChangePassword());

        return response;
    }

    // ───────────────────────────── READ ─────────────────────────────
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found by id : " + id));
        return mapToResponseDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> list = new ArrayList<>();
        for (User user : users) {
            list.add(mapToResponseDTO(user));
        }
        return list;
    }

    public UserResponseDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found by email : " + email));
        return mapToResponseDTO(user);
    }

    // ───────────────────────────── UPDATE ─────────────────────────────
    public UserResponseDTO updateUser(UpdateUserRequest req) {
        User user = userRepository.findById(req.getId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found By Id : " + req.getId()));

        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPhoneNumber(req.getPhoneNo());
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        return mapToResponseDTO(saved);
    }

    public UserResponseDTO changePassword(ChangePasswordRequest req) {
        User user = userRepository.findById(req.getId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found By Id : " + req.getId()));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new WrongPasswordException("Wrong password!!");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return mapToResponseDTO(user);
    }

    public User changeProfileImage(Long id, String image) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found By Id : " + id));
        user.setImage(image);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteUserById(id);
    }

    // ───────────────────────────── OTP ─────────────────────────────
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found by email: " + email));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found by email: " + email));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        String newOtp = generateOtp();
        user.setOtp(newOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), user.getName(), newOtp);
    }

    // ───────────────────────────── HELPERS ─────────────────────────────
    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getImage()
        );
    }
}