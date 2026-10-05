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
public class TransactionUpdateRequest {

    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime date;
    private Long categoryId;
    private TransactionType transactionType;

    @JsonProperty("recurring")
    private Boolean isRecurring = false;
    private Integer recurrenceDay;
}