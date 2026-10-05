package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.dto.BankAccountRequest;
import com.Expenzo_Backend.Expenzo.dto.BankAccountResponse;
import com.Expenzo_Backend.Expenzo.dto.BankAccountUpdateRequest;
import com.Expenzo_Backend.Expenzo.model.AccountType;
import com.Expenzo_Backend.Expenzo.model.BankAccount;
import com.Expenzo_Backend.Expenzo.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/bank-account")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }
    @PostMapping("/add-bank-account")
    public BankAccountResponse addBankAccount(
            @RequestBody BankAccountRequest request,
            Principal principal) {
        return bankAccountService.addBankAccount(request, principal.getName());
    }

    @PutMapping("/update-bank-account/{id}")
    public BankAccountResponse updateBankAccount(
            @PathVariable Long id,
            @RequestBody BankAccountUpdateRequest request,
            Principal principal) {
        return bankAccountService.updateBankAccount(id, request, principal.getName());
    }

    @GetMapping("/{id}")
    public BankAccountResponse getBankDetailsById(
            @PathVariable Long id,
            Principal principal) {
        return bankAccountService.getBankDetailsById(id, principal.getName());
    }

    @GetMapping("/all-bank-accounts")
    public List<BankAccountResponse> getAllBankDetails(Principal principal) {
        return bankAccountService.getAllBankDetails(principal.getName());
    }

    @PutMapping("/change-account-type/{id}")
    public BankAccountResponse changeAccountType(
            @PathVariable Long id,
            @RequestBody AccountType newAccountType,
            Principal principal) {
        return bankAccountService.changeAccountType(id, newAccountType, principal.getName());
    }

    @PutMapping("/update-owner-name/{id}")
    public BankAccountResponse updateOwnerName(
            @PathVariable Long id,
            @RequestParam String newOwnerName,
            Principal principal) {
        return bankAccountService.updateOwnerName(id, newOwnerName, principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBankAccount(
            @PathVariable Long id,
            Principal principal) {
        bankAccountService.deleteBankAccount(id, principal.getName());
        return ResponseEntity.ok().build();
    }
}
