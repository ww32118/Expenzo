package com.Expenzo_Backend.Expenzo.dto;

import com.Expenzo_Backend.Expenzo.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountResponse {

    private Long id;
    private String bankName;
    private String bankAccountNumber;
    private String accountHolderName;
    private BigDecimal balance;
    private String ifscCode;
    private AccountType accountType;
    private Long userId;
    private String userName;

}
