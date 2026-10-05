package com.Expenzo_Backend.Expenzo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    private Long id;
    private String name;
    private String email;
    private String username;
    private String phoneNo;
}
