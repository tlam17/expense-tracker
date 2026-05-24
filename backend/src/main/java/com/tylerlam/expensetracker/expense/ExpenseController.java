package com.tylerlam.expensetracker.expense;

import java.time.YearMonth;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tylerlam.expensetracker.expense.dto.ExpenseRequest;
import com.tylerlam.expensetracker.expense.dto.ExpenseResponse;
import com.tylerlam.expensetracker.shared.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // GET /api/expenses - Get all expenses
    @GetMapping()
    public ResponseEntity<PageResponse<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) YearMonth month, 
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ExpenseResponse> responses = expenseService.getAllExpenses(month, categoryId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    // GET /api/expenses/{id} - Get expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // POST /api/expenses - Create a new expense
    @PostMapping()
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody @Valid ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/expenses/{id} - Update an existing expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense (@PathVariable Long id, @RequestBody @Valid ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // DELETE /api/expenses/{id} - Delete an expense
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
