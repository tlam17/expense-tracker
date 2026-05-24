package com.tylerlam.expensetracker.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;
import com.tylerlam.expensetracker.expense.dto.ExpenseRequest;
import com.tylerlam.expensetracker.expense.dto.ExpenseResponse;
import com.tylerlam.expensetracker.shared.dto.PageResponse;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Category buildCategory(Long id, String name, BigDecimal budget) {
        return Category.builder()
                .id(id)
                .name(name)
                .defaultBudget(budget)
                .build();
    }

    private Expense buildExpense(Long id, BigDecimal amount, LocalDate date, String description, Category category) {
        return Expense.builder()
                .id(id)
                .amount(amount)
                .date(date)
                .description(description)
                .category(category)
                .build();
    }

    private ExpenseRequest buildRequest(BigDecimal amount, LocalDate date, String description, Long categoryId) {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(amount);
        request.setDate(date);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        return request;
    }

    // --- getAllExpenses ---

    @Test
    public void getAllExpenses_withNoFilters_returnsAllExpenses() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        List<Expense> expenses = List.of(
                buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category),
                buildExpense(2L, new BigDecimal("100.00"), LocalDate.of(2024, 1, 11), "Groceries", category)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> page = new PageImpl<>(expenses, pageable, expenses.size());
        when(expenseRepository.findAll(ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<ExpenseResponse> result = expenseService.getAllExpenses(null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getContent().get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Lunch");
        assertThat(result.getContent().get(0).getCategory().getId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        assertThat(result.getContent().get(1).getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    public void getAllExpenses_withNoFilters_returnsEmptyListWhenNoneExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> page = new PageImpl<>(List.of(), pageable, 0);
        when(expenseRepository.findAll(ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<ExpenseResponse> result = expenseService.getAllExpenses(null, null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    public void getAllExpenses_withMonthFilter_returnsFilteredExpenses() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        List<Expense> expenses = List.of(
                buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> page = new PageImpl<>(expenses, pageable, expenses.size());
        when(expenseRepository.findAll(ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<ExpenseResponse> result = expenseService.getAllExpenses(YearMonth.of(2024, 1), null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 10));
    }

    @Test
    public void getAllExpenses_withCategoryFilter_returnsFilteredExpenses() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        List<Expense> expenses = List.of(
                buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> page = new PageImpl<>(expenses, pageable, expenses.size());
        when(expenseRepository.findAll(ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<ExpenseResponse> result = expenseService.getAllExpenses(null, 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory().getId()).isEqualTo(1L);
    }

    @Test
    public void getAllExpenses_withMonthAndCategoryFilter_returnsFilteredExpenses() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        List<Expense> expenses = List.of(
                buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> page = new PageImpl<>(expenses, pageable, expenses.size());
        when(expenseRepository.findAll(ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<ExpenseResponse> result = expenseService.getAllExpenses(YearMonth.of(2024, 1), 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(result.getContent().get(0).getCategory().getId()).isEqualTo(1L);
    }

    @Test
    public void getAllExpenses_returnsCorrectPageMetadata() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        List<Expense> expenses = List.of(
                buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category)
        );
        Pageable pageable = PageRequest.of(1, 10);
        Page<Expense> page = new PageImpl<>(expenses, pageable, 11);
        when(expenseRepository.findAll(ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class))).thenReturn(page);

        PageResponse<ExpenseResponse> result = expenseService.getAllExpenses(null, null, pageable);

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(11L);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    // --- getExpenseById ---

    @Test
    public void getExpenseById_returnsExpense_whenFound() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Expense expense = buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        ExpenseResponse result = expenseService.getExpenseById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(result.getDescription()).isEqualTo("Lunch");
        assertThat(result.getCategory().getId()).isEqualTo(1L);
        assertThat(result.getCategory().getName()).isEqualTo("Food");
        assertThat(result.getCategory().getDefaultBudget()).isEqualByComparingTo("500.00");
    }

    @Test
    public void getExpenseById_throwsResourceNotFoundException_whenNotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with id: 99");
    }

    // --- createExpense ---

    @Test
    public void createExpense_savesAndReturnsExpense() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", 1L);
        Expense saved = buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ExpenseResponse result = expenseService.createExpense(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(result.getDescription()).isEqualTo("Lunch");
        assertThat(result.getCategory().getId()).isEqualTo(1L);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("25.00");
        assertThat(captor.getValue().getDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(captor.getValue().getDescription()).isEqualTo("Lunch");
        assertThat(captor.getValue().getCategory()).isEqualTo(category);
    }

    @Test
    public void createExpense_savesAndReturnsExpense_withNullDescription() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), null, 1L);
        Expense saved = buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), null, category);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ExpenseResponse result = expenseService.createExpense(request);

        assertThat(result.getDescription()).isNull();
    }

    @Test
    public void createExpense_throwsResourceNotFoundException_whenCategoryNotFound() {
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(expenseRepository, never()).save(any());
    }

    // --- updateExpense ---

    @Test
    public void updateExpense_updatesAndReturnsExpense_whenFound() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Expense existing = buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 1L);
        Expense updated = buildExpense(1L, new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", category);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(existing)).thenReturn(updated);

        ExpenseResponse result = expenseService.updateExpense(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("50.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(result.getDescription()).isEqualTo("Dinner");
        assertThat(existing.getAmount()).isEqualByComparingTo("50.00");
        assertThat(existing.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(existing.getDescription()).isEqualTo("Dinner");
    }

    @Test
    public void updateExpense_throwsResourceNotFoundException_whenExpenseNotFound() {
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 1L);
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with id: 99");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    public void updateExpense_throwsResourceNotFoundException_whenCategoryNotFound() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Expense existing = buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 99L);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with id: 99");

        verify(expenseRepository, never()).save(any());
    }

    // --- deleteExpense ---

    @Test
    public void deleteExpense_deletesExpense_whenFound() {
        Category category = buildCategory(1L, "Food", new BigDecimal("500.00"));
        Expense expense = buildExpense(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(1L);

        verify(expenseRepository).delete((Expense) expense);
    }

    @Test
    public void deleteExpense_throwsResourceNotFoundException_whenNotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with id: 99");

        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
