package com.Expenzo_Backend.Expenzo.dto;

import com.Expenzo_Backend.Expenzo.model.Category;
import com.Expenzo_Backend.Expenzo.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Year;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequest {

    private Long id;
    private BigDecimal monthlyLimit;
    private String month;
    private int year;
    private Long userId;
    private Long categoryId;

}
