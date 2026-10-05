package com.Expenzo_Backend.Expenzo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportTransactionDTO {

    private int rowNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String date;          // raw string from file — parsed in service
    private String categoryName;  // matched to user's category by name
    private String transactionType; // "CREDIT" or "DEBIT"
    private String bankAccountId; // optional — user selects default in frontend
    private boolean valid = true;
    private String errorMessage;
}
