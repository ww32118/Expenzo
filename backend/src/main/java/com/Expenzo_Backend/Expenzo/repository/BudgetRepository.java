package com.Expenzo_Backend.Expenzo.repository;

import com.Expenzo_Backend.Expenzo.model.Budget;
import com.Expenzo_Backend.Expenzo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {

    Long id(Long id);

    Budget findBudgetById(Long id);

    Collection<Budget> findByUserId(Long id);

    // Get all budgets for a user, latest year first
    List<Budget> findByUserIdOrderByYearDescIdDesc(Long userId);

    // Find specific budget for user + month + year
    Optional<Budget> findByUserIdAndMonthAndYear(Long userId, String month, int year);

    // Custom method for duplicate check (handles null categoryId)
    @Query("SELECT COUNT(b) > 0 FROM Budget b " +
            "WHERE b.user.id = :userId " +
            "AND b.month = :month " +
            "AND b.year = :year " +
            "AND (:categoryId IS NULL OR b.category.id = :categoryId)")
    boolean existsByUserIdAndMonthAndYearAndCategoryId(
            @Param("userId") Long userId,
            @Param("month") String month,
            @Param("year") int year,
            @Param("categoryId") Long categoryId
    );

    // Optional: Get budgets by category
    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);
}
