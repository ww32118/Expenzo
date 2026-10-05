package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.dto.BudgetRequest;
import com.Expenzo_Backend.Expenzo.dto.BudgetResponse;
import com.Expenzo_Backend.Expenzo.dto.BudgetUpdateRequest;
import com.Expenzo_Backend.Expenzo.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/add-budget")
    public BudgetResponse addBudget(
            @RequestBody BudgetRequest request,
            Principal principal) {
        return budgetService.addBudget(request, principal.getName());
    }

    @PutMapping("/update-budget/{id}")
    public BudgetResponse updateBudget(
            @PathVariable Long id,
            @RequestBody BudgetUpdateRequest request,
            Principal principal) {
        return budgetService.updateBudget(id, request, principal.getName());
    }

    @GetMapping("/{id}")
    public BudgetResponse getBudgetById(
            @PathVariable Long id,
            Principal principal) {
        return budgetService.getBudgetById(id, principal.getName());
    }

    @GetMapping("/all-budgets")
    public List<BudgetResponse> getAllBudgets(Principal principal) {
        return budgetService.getAllBudgets(principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(
            @PathVariable Long id,
            Principal principal) {
        budgetService.deleteBudget(id, principal.getName());
        return ResponseEntity.ok().build();
    }
}
