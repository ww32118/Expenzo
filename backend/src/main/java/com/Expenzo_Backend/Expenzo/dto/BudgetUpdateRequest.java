package com.Expenzo_Backend.Expenzo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetUpdateRequest {

    private BigDecimal monthlyLimit;
    private String month;
    private Integer year;      // Integer (not int) to allow null
    private Long categoryId;
}