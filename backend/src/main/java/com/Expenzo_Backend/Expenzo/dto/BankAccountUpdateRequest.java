package com.Expenzo_Backend.Expenzo.dto;

import com.Expenzo_Backend.Expenzo.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountUpdateRequest {

    private String bankName;
    private String bankAccountNumber;
    private String accountHolderName;
    private String ifscCode;
    private BigDecimal balance;
    private AccountType accountType;
}