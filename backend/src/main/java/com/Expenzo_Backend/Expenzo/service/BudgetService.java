package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.dto.BudgetRequest;
import com.Expenzo_Backend.Expenzo.dto.BudgetResponse;
import com.Expenzo_Backend.Expenzo.dto.BudgetUpdateRequest;
//import com.Expenzo_Backend.Expenzo.exception.BudgetNotFoundException;
import com.Expenzo_Backend.Expenzo.model.Budget;
import com.Expenzo_Backend.Expenzo.model.Category;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.BudgetRepository;
import com.Expenzo_Backend.Expenzo.repository.CategoryRepository;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
//import java.util.ArrayList;
import java.util.List;
//import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetResponse addBudget(BudgetRequest request, String email) {

        // 1. Get logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // 2. Validate inputs
        if (request.getMonthlyLimit() == null) {
            throw new RuntimeException("Monthly limit is required");
        }
        if (request.getMonth() == null || request.getMonth().trim().isEmpty()) {
            throw new RuntimeException("Month is required");
        }
        if (request.getYear() <= 0) {
            throw new RuntimeException("Valid year is required");
        }

        // 3. Prevent duplicate budgets (same month+year+category for same user)
        boolean exists = budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(
                user.getId(),
                request.getMonth(),
                request.getYear(),
                request.getCategoryId()
        );
        if (exists) {
            throw new RuntimeException("Budget already exists for this month/category");
        }

        // 4. Get category (optional)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // 🔒 Verify category belongs to user
            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Category doesn't belong to you");
            }
        }

        // 5. Create budget
        Budget budget = new Budget();
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonth(request.getMonth().toUpperCase());
        budget.setYear(request.getYear());
        budget.setCategory(category);
        budget.setUser(user); // 🔑 Link to user

        return mapToResponse(budgetRepository.save(budget));
    }

    // ═══════════════════════════════════════════════
    // UPDATE BUDGET
    // ═══════════════════════════════════════════════
    public BudgetResponse updateBudget(Long id, BudgetUpdateRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // 🔒 Security check
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to update this budget");
        }

        // Partial update
        if (request.getMonthlyLimit() != null) {
            budget.setMonthlyLimit(request.getMonthlyLimit());
        }
        if (request.getMonth() != null) {
            budget.setMonth(request.getMonth().toUpperCase());
        }
        if (request.getYear() != null && request.getYear() > 0) {
            budget.setYear(request.getYear());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            if (!category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Category doesn't belong to you");
            }
            budget.setCategory(category);
        }

        return mapToResponse(budgetRepository.save(budget));
    }

    // ═══════════════════════════════════════════════
    // GET BUDGET BY ID
    // ═══════════════════════════════════════════════
    public BudgetResponse getBudgetById(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // 🔒 Security check
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        return mapToResponse(budget);
    }

    // ═══════════════════════════════════════════════
    // GET ALL BUDGETS (for current user only)
    // ═══════════════════════════════════════════════
    public List<BudgetResponse> getAllBudgets(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return budgetRepository.findByUserIdOrderByYearDescIdDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════
    // DELETE BUDGET
    // ═══════════════════════════════════════════════
    public void deleteBudget(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // 🔒 Security check
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this budget");
        }

        budgetRepository.delete(budget);
    }

    // ═══════════════════════════════════════════════
    // HELPER: Entity → DTO
    // ═══════════════════════════════════════════════
    private BudgetResponse mapToResponse(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setMonthlyLimit(budget.getMonthlyLimit());
        response.setMonth(budget.getMonth());
        response.setYear(budget.getYear());

        // ✅ Null-safe user mapping
        if (budget.getUser() != null) {
            response.setUserId(budget.getUser().getId());
            response.setUserName(
                    budget.getUser().getUsername() != null
                            ? budget.getUser().getUsername()
                            : budget.getUser().getEmail()
            );
        }

        // ✅ Null-safe category mapping
        if (budget.getCategory() != null) {
            response.setCategoryId(budget.getCategory().getId());
            response.setCategoryName(budget.getCategory().getName());
        }

        return response;
    }
}
