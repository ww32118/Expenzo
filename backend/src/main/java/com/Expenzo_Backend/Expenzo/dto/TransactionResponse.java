package com.Expenzo_Backend.Expenzo.dto;

import com.Expenzo_Backend.Expenzo.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime date;

    private Long userId;
    private String userName;

    private Long categoryId;
    private String categoryName;

    private Long bankAccountId;
    private String bankName;
    private String bankAccountNumber;

    private TransactionType transactionType;

    @JsonProperty("recurring")
    private Boolean recurring = false;
    private Integer recurrenceDay;
}