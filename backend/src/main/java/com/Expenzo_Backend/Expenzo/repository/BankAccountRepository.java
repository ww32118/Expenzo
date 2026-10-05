package com.Expenzo_Backend.Expenzo.repository;

import com.Expenzo_Backend.Expenzo.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Long> {

    Long id(Long id);

    BankAccount findBankAccountById(Long id);

    boolean existsByUserIdAndBankAccountNumber(Long userId, String accountNumber);

    Collection<BankAccount> findByUserId(Long id);

}
