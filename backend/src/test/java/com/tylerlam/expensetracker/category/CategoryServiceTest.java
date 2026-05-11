package com.tylerlam.expensetracker.category;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tylerlam.expensetracker.category.dto.CategoryRequest;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.expense.ExpenseRepository;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category buildCategory(Long id, String name, BigDecimal budget) {
        return Category.builder()
                .id(id)
                .name(name)
                .defaultBudget(budget)
                .build();
    }

    private CategoryRequest buildRequest(String name, BigDecimal budget) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setDefaultBudget(budget);
        return request;
    }

    // --- getAllCategories ---

    @Test
    public void getAllCategories_returnsAllCategories() {
        List<Category> categories = List.of(
                buildCategory(1L, "Food", new BigDecimal("500.00")),
                buildCategory(2L, "Rent", new BigDecimal("1500.00"))
        );
        when(categoryRepository.findAll()).thenReturn(categories);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Food");
        assertThat(result.get(0).getDefaultBudget()).isEqualByComparingTo("500.00");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Rent");
        assertThat(result.get(1).getDefaultBudget()).isEqualByComparingTo("1500.00");
    }

    @Test
    public void getAllCategories_returnsEmptyListWhenNoneExist() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).isEmpty();
    }

    // --- getCategoryById ---

    @Test
    public void getCategoryById_returnsCategory_whenFound() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getDefaultBudget()).isEqualByComparingTo("500.00");
    }

    @Test
    public void getCategoryById_throwsResourceNotFoundException_whenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");
    }

    // --- createCategory ---

    @Test
    public void createCategory_savesAndReturnsCategory() {
        CategoryRequest request = buildRequest("Food", new BigDecimal("500.00"));
        Category saved = buildCategory(1L, "Food", new BigDecimal("500.00"));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getDefaultBudget()).isEqualByComparingTo("500.00");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Food");
        assertThat(captor.getValue().getDefaultBudget()).isEqualByComparingTo("500.00");
    }

    @Test
    public void createCategory_savesAndReturnsCategory_withNullBudget() {
        CategoryRequest request = buildRequest("Miscellaneous", null);
        Category saved = buildCategory(1L, "Miscellaneous", null);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Miscellaneous");
        assertThat(result.getDefaultBudget()).isNull();
    }

    // --- updateCategory ---

    @Test
    public void updateCategory_updatesAndReturnsCategory_whenFound() {
        Category existing = buildCategory(1L, "Food", new BigDecimal("500.00"));
        CategoryRequest request = buildRequest("Updated Food", new BigDecimal("600.00"));
        Category updated = buildCategory(1L, "Updated Food", new BigDecimal("600.00"));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(updated);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Updated Food");
        assertThat(result.getDefaultBudget()).isEqualByComparingTo("600.00");
        assertThat(existing.getName()).isEqualTo("Updated Food");
        assertThat(existing.getDefaultBudget()).isEqualByComparingTo("600.00");
    }

    @Test
    public void updateCategory_throwsResourceNotFoundException_whenNotFound() {
        CategoryRequest request = buildRequest("Updated Food", new BigDecimal("600.00"));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(categoryRepository, never()).save(any());
    }

    // --- deleteCategory ---

    @Test
    public void deleteCategory_deletesCategory_whenFound() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    public void deleteCategory_throwsResourceNotFoundException_whenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(categoryRepository, never()).delete(any());
    }
}
