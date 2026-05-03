package com.tylerlam.expensetracker.category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tylerlam.expensetracker.category.dto.CategoryRequest;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories - Get all categories
    @GetMapping()
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> responses = categoryService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    // GET /api/categories/{id} - Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // POST /api/categories - Create a new category
    @PostMapping()
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/categories/{id} - Update an existing category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory (@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // DELETE /api/categories/{id} - Delete a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
