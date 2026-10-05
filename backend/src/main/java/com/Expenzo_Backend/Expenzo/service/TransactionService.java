package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.dto.TransactionRequest;
import com.Expenzo_Backend.Expenzo.dto.TransactionResponse;
import com.Expenzo_Backend.Expenzo.dto.TransactionUpdateRequest;
import com.Expenzo_Backend.Expenzo.exception.BankAccountDoesNotExistsException;
import com.Expenzo_Backend.Expenzo.model.BankAccount;
import com.Expenzo_Backend.Expenzo.model.Category;
import com.Expenzo_Backend.Expenzo.model.Transaction;
import com.Expenzo_Backend.Expenzo.model.TransactionType;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.BankAccountRepository;
import com.Expenzo_Backend.Expenzo.repository.CategoryRepository;
import com.Expenzo_Backend.Expenzo.repository.TransactionRepository;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;

    // ───────────────────────────── ADD ─────────────────────────────
    public TransactionResponse addTransaction(TransactionRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (request.getAmount() == null) {
            throw new RuntimeException("Amount is required");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Category doesn't belong to you");
            }
        }

        BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new BankAccountDoesNotExistsException("Bank account not found"));

        // CREDIT = income → balance increases | DEBIT = expense → balance decreases
        if (request.getTransactionType() == TransactionType.CREDIT) {
            bankAccount.setBalance(bankAccount.getBalance().add(request.getAmount()));
        } else {
            bankAccount.setBalance(bankAccount.getBalance().subtract(request.getAmount()));
        }
        bankAccountRepository.save(bankAccount);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setCurrency(request.getCurrency());
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setBankAccount(bankAccount);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setIsRecurring(request.getIsRecurring());        // ✅ Boolean getter
        transaction.setRecurrenceDay(request.getRecurrenceDay());

        return mapToResponse(transactionRepository.save(transaction));
    }

    // ───────────────────────────── UPDATE ─────────────────────────────
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to update this transaction");
        }

        BankAccount bankAccount = transaction.getBankAccount();

        if (request.getAmount() != null) {
            // Step 1: Reverse the old transaction's effect
            if (transaction.getTransactionType() == TransactionType.CREDIT) {
                bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getAmount()));
            } else {
                bankAccount.setBalance(bankAccount.getBalance().add(transaction.getAmount()));
            }

            // Step 2: Apply the new amount with the (possibly new) type
            TransactionType newType = request.getTransactionType() != null
                    ? request.getTransactionType()
                    : transaction.getTransactionType();

            if (newType == TransactionType.CREDIT) {
                bankAccount.setBalance(bankAccount.getBalance().add(request.getAmount()));
            } else {
                bankAccount.setBalance(bankAccount.getBalance().subtract(request.getAmount()));
            }

            bankAccountRepository.save(bankAccount);
            transaction.setAmount(request.getAmount());
        }

        if (request.getTransactionType() != null) {
            transaction.setTransactionType(request.getTransactionType());
        }
        if (request.getCurrency() != null) {
            transaction.setCurrency(request.getCurrency());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getRecurrenceDay() != null) {
            transaction.setRecurrenceDay(request.getRecurrenceDay());
        }
        if (request.getIsRecurring() != null) {                    // ✅ null-safe (Boolean wrapper)
            transaction.setIsRecurring(request.getIsRecurring());
        }

        return mapToResponse(transactionRepository.save(transaction));
    }

    // ───────────────────────────── READ ─────────────────────────────
    public TransactionResponse getTransactionById(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getAllTransactions(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ───────────────────────────── DELETE ─────────────────────────────
    @Transactional
    public void deleteTransaction(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security check BEFORE reversing balance
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this transaction");
        }

        // Reverse balance change based on original transaction type
        BankAccount bankAccount = transaction.getBankAccount();
        if (transaction.getTransactionType() == TransactionType.CREDIT) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getAmount()));
        } else {
            bankAccount.setBalance(bankAccount.getBalance().add(transaction.getAmount()));
        }
        bankAccountRepository.save(bankAccount);

        transactionRepository.delete(transaction);
    }

    // ───────────────────────────── MAPPER ─────────────────────────────
    private TransactionResponse mapToResponse(Transaction t) {
        TransactionResponse response = new TransactionResponse();
        response.setId(t.getId());
        response.setAmount(t.getAmount());
        response.setCurrency(t.getCurrency());
        response.setDescription(t.getDescription());
        response.setDate(t.getDate());

        if (t.getUser() != null) {
            response.setUserId(t.getUser().getId());
            response.setUserName(
                    t.getUser().getUsername() != null
                            ? t.getUser().getUsername()
                            : t.getUser().getEmail()
            );
        }

        if (t.getCategory() != null) {
            response.setCategoryId(t.getCategory().getId());
            response.setCategoryName(t.getCategory().getName());
        }

        if (t.getBankAccount() != null) {
            response.setBankAccountId(t.getBankAccount().getId());
            response.setBankName(t.getBankAccount().getBankName());
            response.setBankAccountNumber(t.getBankAccount().getBankAccountNumber());
        }

        response.setTransactionType(t.getTransactionType());
        response.setRecurring(t.getIsRecurring());                // ✅ Boolean getter
        response.setRecurrenceDay(t.getRecurrenceDay());

        return response;
    }
}