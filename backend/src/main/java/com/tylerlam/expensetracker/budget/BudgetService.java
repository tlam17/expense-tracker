package com.tylerlam.expensetracker.budget;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tylerlam.expensetracker.budget.dto.BudgetRequest;
import com.tylerlam.expensetracker.budget.dto.BudgetResponse;
import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    // Get all budgets (add filtering later)
    public List<BudgetResponse> getAllBudgets() {
        List<Budget> budgets = budgetRepository.findAll();
        return budgets.stream()                
                .map(this::toResponse)
                .toList();
    }

    // Get budget by ID
    public BudgetResponse getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        return toResponse(budget);
    }

    // Create a new budget
    public BudgetResponse createBudget(BudgetRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .month(request.getMonth())
                .category(category)
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        return toResponse(savedBudget);
    }

    // Update an existing budget
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        budget.setMonth(request.getMonth());
        budget.setAmount(request.getAmount());
        budget.setCategory(category);

        Budget updatedBudget = budgetRepository.save(budget);
        return toResponse(updatedBudget);
    }

    // Delete a budget
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        budgetRepository.delete(budget);
    }

    // Helper method to convert Budget entity to BudgetResponse DTO
    private BudgetResponse toResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .category(toResponse(budget.getCategory()))
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
