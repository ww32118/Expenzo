package com.Expenzo_Backend.Expenzo.scheduler;

import com.Expenzo_Backend.Expenzo.model.BankAccount;
import com.Expenzo_Backend.Expenzo.model.Transaction;
import com.Expenzo_Backend.Expenzo.model.TransactionType;
import com.Expenzo_Backend.Expenzo.repository.BankAccountRepository;
import com.Expenzo_Backend.Expenzo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecurringTransactionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BankAccountRepository bankAccountRepository;

    // Testing ke liye — har minute chalega
    @Scheduled(cron = "0 * * * * *")
    public void processRecurringTransactions() {

        int todayDate = LocalDateTime.now().getDayOfMonth();
        log.info("Recurring scheduler chala — aaj ki tarikh: {}", todayDate);

        List<Transaction> recurringList = transactionRepository
                .findByIsRecurringTrueAndRecurrenceDay(todayDate);

        if (recurringList.isEmpty()) {
            log.info("Aaj koi recurring transaction nahi hai.");
            return;
        }

        for (Transaction original : recurringList) {
            try {

                Transaction newTx = new Transaction();
                newTx.setAmount(original.getAmount());
                newTx.setCurrency(original.getCurrency());
                newTx.setDescription("[Auto] " + original.getDescription());
                newTx.setDate(LocalDateTime.now());
                newTx.setUser(original.getUser());
                newTx.setCategory(original.getCategory());
                newTx.setBankAccount(original.getBankAccount());
                newTx.setTransactionType(original.getTransactionType());
                newTx.setIsRecurring(false);
                newTx.setRecurrenceDay(null);

                BankAccount bankAccount = original.getBankAccount();
                if (original.getTransactionType() == TransactionType.CREDIT) {
                    bankAccount.setBalance(bankAccount.getBalance().add(original.getAmount()));
                } else {
                    bankAccount.setBalance(bankAccount.getBalance().subtract(original.getAmount()));
                }
                bankAccountRepository.save(bankAccount);
                transactionRepository.save(newTx);

                log.info("Auto transaction create hua: {} — ₹{} ({})",
                        newTx.getDescription(), newTx.getAmount(), newTx.getTransactionType());

            } catch (Exception e) {
                log.error("Recurring transaction process karne mein error — ID {}: {}",
                        original.getId(), e.getMessage());
            }
        }
    }
}