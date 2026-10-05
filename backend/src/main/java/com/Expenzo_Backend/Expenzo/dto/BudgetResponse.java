package com.Expenzo_Backend.Expenzo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {

    private Long id;
    private BigDecimal monthlyLimit;
    private String month;
    private int year;

    private Long userId;
    private String userName;

    // ✅ Added category info (useful for UI)
    private Long categoryId;
    private String categoryName;
}