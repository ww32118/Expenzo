package com.Expenzo_Backend.Expenzo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    private String name;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String image;

}
