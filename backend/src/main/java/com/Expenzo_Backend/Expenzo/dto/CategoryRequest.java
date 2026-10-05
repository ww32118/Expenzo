package com.Expenzo_Backend.Expenzo.dto;

import com.Expenzo_Backend.Expenzo.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    private Long id;
    private String name;
    private Long userId;

}
