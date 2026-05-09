package com.tylerlam.expensetracker.category;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tylerlam.expensetracker.category.dto.CategoryRequest;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.expense.ExpenseRepository;
import com.tylerlam.expensetracker.shared.exception.CategoryInUseException;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    // Get all categories
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    // Get category by ID
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return toResponse(category);
    }

    // Create a new category
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .defaultBudget(request.getDefaultBudget())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    // Update an existing category
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(request.getName());
        category.setDefaultBudget(request.getDefaultBudget());

        Category updatedCategory = categoryRepository.save(category);

        return toResponse(updatedCategory);
    }

    // Delete a category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if category is associated with any expenses before deleting
        if (expenseRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("Cannot delete category with id " + id + " because it is associated with existing expenses.");
        }

        categoryRepository.delete(category);
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
