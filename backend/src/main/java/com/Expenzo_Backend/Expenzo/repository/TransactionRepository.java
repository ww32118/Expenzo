package com.Expenzo_Backend.Expenzo.repository;

import com.Expenzo_Backend.Expenzo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction getTransactionById(Long id);

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId GROUP BY t.category.name")
    List<Object[]> getCategoryWiseSpending(@Param("userId") Long userId);

    List<Transaction> findByUserId(Long userId);

    Collection<Transaction> findByUserIdOrderByDateDesc(Long id);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByIsRecurringTrueAndRecurrenceDay(int todayDate);
}
