package com.Expenzo_Backend.Expenzo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private String image;
    private LocalDateTime createdAt;

    private boolean mustChangePassword;

}