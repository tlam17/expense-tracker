package com.tylerlam.expensetracker.expense;

import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
public class ExpenseRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category food;
    private Category rent;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        food = categoryRepository.save(Category.builder().name("Food").build());
        rent = categoryRepository.save(Category.builder().name("Rent").build());
    }

    private Expense savedExpense(BigDecimal amount, LocalDate date, Category category, String description) {
        return expenseRepository.save(
                Expense.builder()
                        .amount(amount)
                        .date(date)
                        .category(category)
                        .description(description)
                        .build());
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    @Test
    void save_persistsExpense() {
        Expense saved = savedExpense(
                new BigDecimal("42.50"),
                LocalDate.of(2026, 4, 9),
                food,
                "Grocery run");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("42.50");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 4, 9));
        assertThat(saved.getDescription()).isEqualTo("Grocery run");
        assertThat(saved.getCategory().getId()).isEqualTo(food.getId());
    }

    @Test
    void save_setsCreatedAtAutomatically() {
        Expense saved = savedExpense(new BigDecimal("10.00"), LocalDate.now(), food, null);

        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void save_allowsNullDescription() {
        Expense saved = savedExpense(new BigDecimal("10.00"), LocalDate.now(), food, null);

        assertThat(saved.getDescription()).isNull();
    }

    // ── FindById ──────────────────────────────────────────────────────────────

    @Test
    void findById_returnsExpense_whenExists() {
        Expense saved = savedExpense(new BigDecimal("100.00"), LocalDate.now(), rent, "Monthly rent");

        Optional<Expense> found = expenseRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<Expense> found = expenseRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    // ── ExistsByCategoryId ────────────────────────────────────────────────────

    @Test
    void existsByCategoryId_returnsTrue_whenExpenseExistsForCategory() {
        savedExpense(new BigDecimal("50.00"), LocalDate.now(), food, null);

        assertThat(expenseRepository.existsByCategoryId(food.getId())).isTrue();
    }

    @Test
    void existsByCategoryId_returnsFalse_whenNoExpenseForCategory() {
        // rent has no expenses
        assertThat(expenseRepository.existsByCategoryId(rent.getId())).isFalse();
    }

    @Test
    void existsByCategoryId_returnsFalse_whenCategoryIdDoesNotExist() {
        assertThat(expenseRepository.existsByCategoryId(999L)).isFalse();
    }

    // ── FindAll (paginated) ───────────────────────────────────────────────────

    @Test
    void findAll_paginated_returnsCorrectPage() {
        savedExpense(new BigDecimal("10.00"), LocalDate.now(), food, "A");
        savedExpense(new BigDecimal("20.00"), LocalDate.now(), food, "B");
        savedExpense(new BigDecimal("30.00"), LocalDate.now(), rent, "C");

        Page<Expense> page = expenseRepository.findAll(PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Test
    void save_updatesExistingExpense() {
        Expense saved = savedExpense(new BigDecimal("42.50"), LocalDate.now(), food, "Old");
        saved.setAmount(new BigDecimal("99.99"));
        saved.setDescription("Updated");
        expenseRepository.save(saved);

        Expense updated = expenseRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getAmount()).isEqualByComparingTo("99.99");
        assertThat(updated.getDescription()).isEqualTo("Updated");
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    void deleteById_removesExpense() {
        Expense saved = savedExpense(new BigDecimal("15.00"), LocalDate.now(), food, null);

        expenseRepository.deleteById(saved.getId());

        assertThat(expenseRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void existsByCategoryId_returnsFalse_afterExpenseDeleted() {
        Expense saved = savedExpense(new BigDecimal("15.00"), LocalDate.now(), food, null);
        expenseRepository.deleteById(saved.getId());

        assertThat(expenseRepository.existsByCategoryId(food.getId())).isFalse();
    }
}