package com.tylerlam.expensetracker.budget;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tylerlam.expensetracker.budget.dto.BudgetRequest;
import com.tylerlam.expensetracker.budget.dto.BudgetResponse;
import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;
import com.tylerlam.expensetracker.shared.exception.DuplicateBudgetException;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetService budgetService;

    private static final YearMonth APR_2026 = YearMonth.of(2026, 4);
    private static final YearMonth MAY_2026 = YearMonth.of(2026, 5);

    private Category buildCategory(Long id, String name, BigDecimal defaultBudget) {
        return Category.builder()
                .id(id)
                .name(name)
                .defaultBudget(defaultBudget)
                .build();
    }

    private Budget buildBudget(Long id, BigDecimal amount, YearMonth month, Category category) {
        return Budget.builder()
                .id(id)
                .amount(amount)
                .month(month)
                .category(category)
                .build();
    }

    private BudgetRequest buildRequest(BigDecimal amount, YearMonth month, Long categoryId) {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(amount);
        request.setMonth(month);
        request.setCategoryId(categoryId);
        return request;
    }

    // --- getAllBudgets ---

    @Test
    public void getAllBudgets_returnsAllBudgets() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Category rent = buildCategory(2L, "Rent", new BigDecimal("1000.00"));
        List<Budget> budgets = List.of(
                buildBudget(1L, new BigDecimal("500.00"), APR_2026, food),
                buildBudget(2L, new BigDecimal("1000.00"), APR_2026, rent)
        );
        when(budgetRepository.findAll()).thenReturn(budgets);

        List<BudgetResponse> result = budgetService.getAllBudgets();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.get(0).getMonth()).isEqualTo(APR_2026);
        assertThat(result.get(0).getCategory().getId()).isEqualTo(1L);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Food");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.get(1).getCategory().getId()).isEqualTo(2L);
    }

    @Test
    public void getAllBudgets_returnsEmptyList_whenNoneExist() {
        when(budgetRepository.findAll()).thenReturn(List.of());

        List<BudgetResponse> result = budgetService.getAllBudgets();

        assertThat(result).isEmpty();
    }

    // --- getBudgetById ---

    @Test
    public void getBudgetById_returnsBudget_whenFound() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Budget budget = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        BudgetResponse result = budgetService.getBudgetById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getMonth()).isEqualTo(APR_2026);
        assertThat(result.getCategory().getId()).isEqualTo(1L);
        assertThat(result.getCategory().getName()).isEqualTo("Food");
        assertThat(result.getCategory().getDefaultBudget()).isEqualByComparingTo("500.00");
    }

    @Test
    public void getBudgetById_throwsResourceNotFoundException_whenNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Budget not found with id: 99");
    }

    // --- createBudget ---

    @Test
    public void createBudget_savesAndReturnsBudget() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, 1L);
        Budget saved = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(budgetRepository.existsByMonthAndCategoryId(APR_2026, 1L)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(saved);

        BudgetResponse result = budgetService.createBudget(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getMonth()).isEqualTo(APR_2026);
        assertThat(result.getCategory().getId()).isEqualTo(1L);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("500.00");
        assertThat(captor.getValue().getMonth()).isEqualTo(APR_2026);
        assertThat(captor.getValue().getCategory()).isEqualTo(food);
    }

    @Test
    public void createBudget_throwsResourceNotFoundException_whenCategoryNotFound() {
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    public void createBudget_throwsDuplicateBudgetException_whenBudgetAlreadyExists() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(budgetRepository.existsByMonthAndCategoryId(APR_2026, 1L)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(DuplicateBudgetException.class)
                .hasMessage("Budget already exists for month: " + APR_2026 + " and category id: 1");

        verify(budgetRepository, never()).save(any());
    }

    // --- updateBudget ---

    @Test
    public void updateBudget_updatesAndReturnsBudget_whenFound() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Budget existing = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 1L);
        Budget updated = buildBudget(1L, new BigDecimal("750.00"), APR_2026, food);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(budgetRepository.save(existing)).thenReturn(updated);

        BudgetResponse result = budgetService.updateBudget(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("750.00");
        assertThat(result.getMonth()).isEqualTo(APR_2026);
        assertThat(existing.getAmount()).isEqualByComparingTo("750.00");
        assertThat(existing.getCategory()).isEqualTo(food);
    }

    @Test
    public void updateBudget_doesNotCheckDuplicate_whenMonthAndCategoryUnchanged() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Budget existing = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 1L);
        Budget updated = buildBudget(1L, new BigDecimal("750.00"), APR_2026, food);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(budgetRepository.save(existing)).thenReturn(updated);

        budgetService.updateBudget(1L, request);

        verify(budgetRepository, never()).existsByMonthAndCategoryId(any(), any());
    }

    @Test
    public void updateBudget_throwsDuplicateBudgetException_whenChangingMonthToExistingBudget() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Budget existing = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        BudgetRequest request = buildRequest(new BigDecimal("600.00"), MAY_2026, 1L);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(budgetRepository.existsByMonthAndCategoryId(MAY_2026, 1L)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.updateBudget(1L, request))
                .isInstanceOf(DuplicateBudgetException.class)
                .hasMessage("Budget already exists for month: " + MAY_2026 + " and category id: 1");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    public void updateBudget_throwsResourceNotFoundException_whenBudgetNotFound() {
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 1L);
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Budget not found with id: 99");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    public void updateBudget_throwsResourceNotFoundException_whenCategoryNotFound() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Budget existing = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 99L);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(budgetRepository, never()).save(any());
    }

    // --- deleteBudget ---

    @Test
    public void deleteBudget_deletesBudget_whenFound() {
        Category food = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Budget budget = buildBudget(1L, new BigDecimal("500.00"), APR_2026, food);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        budgetService.deleteBudget(1L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    public void deleteBudget_throwsResourceNotFoundException_whenNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.deleteBudget(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Budget not found with id: 99");

        verify(budgetRepository, never()).delete(any(Budget.class));
    }
}
