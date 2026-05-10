package com.tylerlam.expensetracker.expense;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.expense.dto.ExpenseRequest;
import com.tylerlam.expensetracker.expense.dto.ExpenseResponse;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    // Get all expenses (add pagination and filtering later)
    public List<ExpenseResponse> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        return expenses.stream()
                .map(this::toResponse)
                .toList();
    }
    
    // Get expense by ID
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return toResponse(expense);
    }

    // Create a new expense
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .category(category)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        
        return toResponse(savedExpense);
    }

    // Update an existing expense
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);

        Expense updatedExpense = expenseRepository.save(expense);
        return toResponse(updatedExpense);
    }

    // Delete an expense
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expenseRepository.delete(expense);
    }

    // Helper method to convert Expense entity to ExpenseResponse DTO
    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .description(expense.getDescription())
                .category(toResponse(expense.getCategory()))
                .build();
    }

    // Helper method to convert Category entity to CategoryResponse DTO
    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .defaultBudget(category.getDefaultBudget())
                .build();
    }
}
