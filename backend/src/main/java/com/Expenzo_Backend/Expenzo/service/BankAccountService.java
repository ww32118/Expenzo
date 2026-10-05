package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.dto.BankAccountRequest;
import com.Expenzo_Backend.Expenzo.dto.BankAccountResponse;
import com.Expenzo_Backend.Expenzo.dto.BankAccountUpdateRequest;
import com.Expenzo_Backend.Expenzo.exception.BankAccountDoesNotExistsException;
import com.Expenzo_Backend.Expenzo.exception.UserNotFoundException;
import com.Expenzo_Backend.Expenzo.model.AccountType;
import com.Expenzo_Backend.Expenzo.model.BankAccount;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.BankAccountRepository;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              UserRepository userRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    public BankAccountResponse addBankAccount(BankAccountRequest request, String email) {

        // 1. Get logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        // 2. Validate required fields
        validateBankAccountRequest(request);

        // 3. Prevent duplicate account numbers for same user
        if (bankAccountRepository.existsByUserIdAndBankAccountNumber(
                user.getId(), request.getBankAccountNumber())) {
            throw new RuntimeException("You already have this bank account added");
        }

        // 4. Create bank account
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName(request.getBankName());
        bankAccount.setBankAccountNumber(request.getBankAccountNumber());
        bankAccount.setAccountHolderName(request.getAccountHolderName());
        bankAccount.setIfscCode(request.getIfscCode().toUpperCase());
        bankAccount.setBalance(request.getBalance());
        bankAccount.setAccountType(request.getAccountType());
        bankAccount.setUser(user);

        return mapToResponse(bankAccountRepository.save(bankAccount));
    }

    public BankAccountResponse updateBankAccount(Long id, BankAccountUpdateRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountDoesNotExistsException("Bank account not found"));

        // 🔒 Security: only owner can update
        verifyOwnership(bankAccount, user);

        // Partial update — only update non-null fields
        if (request.getBankName() != null) {
            bankAccount.setBankName(request.getBankName());
        }
        if (request.getBankAccountNumber() != null) {
            bankAccount.setBankAccountNumber(request.getBankAccountNumber());
        }
        if (request.getAccountHolderName() != null) {
            bankAccount.setAccountHolderName(request.getAccountHolderName());
        }
        if (request.getIfscCode() != null) {
            bankAccount.setIfscCode(request.getIfscCode().toUpperCase());
        }
        if (request.getAccountType() != null) {
            bankAccount.setAccountType(request.getAccountType());
        }

        if (request.getBalance() != null) {
            bankAccount.setBalance(request.getBalance()); // ← NAYA
        }

        return mapToResponse(bankAccountRepository.save(bankAccount));
    }

    public BankAccountResponse getBankDetailsById(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountDoesNotExistsException("Bank account not found"));

        // 🔒 Security check
        verifyOwnership(bankAccount, user);

        return mapToResponse(bankAccount);
    }

    public List<BankAccountResponse> getAllBankDetails(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return bankAccountRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BankAccountResponse changeAccountType(Long id, AccountType newType, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountDoesNotExistsException("Bank account not found"));

        verifyOwnership(bankAccount, user);

        bankAccount.setAccountType(newType);
        return mapToResponse(bankAccountRepository.save(bankAccount));
    }

    public BankAccountResponse updateOwnerName(Long id, String newOwnerName, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountDoesNotExistsException("Bank account not found"));

        verifyOwnership(bankAccount, user);

        if (newOwnerName == null || newOwnerName.trim().isEmpty()) {
            throw new RuntimeException("Owner name cannot be empty");
        }

        bankAccount.setAccountHolderName(newOwnerName.trim());
        return mapToResponse(bankAccountRepository.save(bankAccount));
    }

    @Transactional
    public void deleteBankAccount(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountDoesNotExistsException("Bank account not found"));

        verifyOwnership(bankAccount, user);

        bankAccountRepository.delete(bankAccount);
    }

    private void validateBankAccountRequest(BankAccountRequest request) {
        if (request.getBankName() == null || request.getBankName().trim().isEmpty()) {
            throw new RuntimeException("Bank name is required");
        }
        if (request.getBankAccountNumber() == null || request.getBankAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (request.getAccountHolderName() == null || request.getAccountHolderName().trim().isEmpty()) {
            throw new RuntimeException("Account holder name is required");
        }
        if (request.getIfscCode() == null || request.getIfscCode().trim().isEmpty()) {
            throw new RuntimeException("IFSC code is required");
        }
        if (request.getAccountType() == null) {
            throw new RuntimeException("Account type is required");
        }
    }

    private void verifyOwnership(BankAccount account, User user) {
        if (account.getUser() == null || !account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to access this bank account");
        }
    }

    private BankAccountResponse mapToResponse(BankAccount account) {
        BankAccountResponse response = new BankAccountResponse();
        response.setId(account.getId());
        response.setBankName(account.getBankName());
        response.setBankAccountNumber(account.getBankAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setIfscCode(account.getIfscCode());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance()); // ← NAYA

        if (account.getUser() != null) {
            response.setUserId(account.getUser().getId());
            response.setUserName(
                    account.getUser().getUsername() != null
                            ? account.getUser().getUsername()
                            : account.getUser().getEmail()
            );
        }

        return response;
    }
}