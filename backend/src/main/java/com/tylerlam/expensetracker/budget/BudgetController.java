package com.tylerlam.expensetracker.budget;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tylerlam.expensetracker.budget.dto.BudgetRequest;
import com.tylerlam.expensetracker.budget.dto.BudgetResponse;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // GET /api/budgets - Get all budgets (add filtering later)
    @GetMapping()
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        List<BudgetResponse> responses = budgetService.getAllBudgets();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    // GET /api/budgets/{id} - Get budget by ID
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        BudgetResponse response = budgetService.getBudgetById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // POST /api/budgets - Create a new budget
    @PostMapping()
    public ResponseEntity<BudgetResponse> createBudget(@RequestBody @Valid BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/budgets/{id} - Update an existing budget
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget (@PathVariable Long id, @RequestBody @Valid BudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // DELETE /api/budgets/{id} - Delete a budget
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
