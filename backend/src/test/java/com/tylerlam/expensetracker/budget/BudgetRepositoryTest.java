package com.tylerlam.expensetracker.budget;

import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
public class BudgetRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category food;
    private Category rent;

    private static final YearMonth APR_2026 = YearMonth.of(2026, 4);
    private static final YearMonth MAY_2026 = YearMonth.of(2026, 5);

    // ── Setup ─────────────────────────────────────────────────────────────────
 
    @BeforeEach
    void setUp() {
        food = categoryRepository.save(Category.builder().name("Food").build());
        rent = categoryRepository.save(Category.builder().name("Rent").build());
    }
 
    private Budget savedBudget(Category category, YearMonth month, BigDecimal amount) {
        return budgetRepository.saveAndFlush(
                Budget.builder()
                        .category(category)
                        .month(month)
                        .amount(amount)
                        .build());
    }
 
    // ── Save ──────────────────────────────────────────────────────────────────
 
    @Test
    void save_persistsBudget() {
        Budget saved = savedBudget(food, APR_2026, new BigDecimal("500.00"));
 
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("500.00");
        assertThat(saved.getMonth()).isEqualTo(APR_2026);
        assertThat(saved.getCategory().getId()).isEqualTo(food.getId());
    }
 
    @Test
    void save_setsCreatedAtAutomatically() {
        Budget saved = savedBudget(food, APR_2026, new BigDecimal("100.00"));
 
        assertThat(saved.getCreatedAt()).isNotNull();
    }
 
    // ── FindById ──────────────────────────────────────────────────────────────
 
    @Test
    void findById_returnsBudget_whenExists() {
        Budget saved = savedBudget(rent, APR_2026, new BigDecimal("1000.00"));
 
        Optional<Budget> found = budgetRepository.findById(saved.getId());
 
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("1000.00");
    }
 
    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<Budget> found = budgetRepository.findById(999L);
 
        assertThat(found).isEmpty();
    }
 
    // ── FindByMonth ───────────────────────────────────────────────────────────
 
    @Test
    void findByMonth_returnsAllBudgetsForThatMonth() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));
        savedBudget(rent, APR_2026, new BigDecimal("1000.00"));
        savedBudget(food, MAY_2026, new BigDecimal("600.00")); // different month
 
        List<Budget> results = budgetRepository.findByMonth(APR_2026);
 
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(b -> b.getMonth().equals(APR_2026));
    }
 
    @Test
    void findByMonth_returnsEmpty_whenNoBudgetsForMonth() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));
 
        List<Budget> results = budgetRepository.findByMonth(MAY_2026);
 
        assertThat(results).isEmpty();
    }
 
    @Test
    void findByMonth_convertsYearMonthCorrectly() {
        savedBudget(food, MAY_2026, new BigDecimal("600.00"));
 
        List<Budget> results = budgetRepository.findByMonth(MAY_2026);
 
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMonth()).isEqualTo(MAY_2026);
    }
 
    // ── FindByCategoryId ──────────────────────────────────────────────────────
 
    @Test
    void findByCategoryId_returnsAllBudgetsForThatCategory() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));
        savedBudget(food, MAY_2026, new BigDecimal("600.00"));
        savedBudget(rent, APR_2026, new BigDecimal("1000.00")); // different category
 
        List<Budget> results = budgetRepository.findByCategoryId(food.getId());
 
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(b -> b.getCategory().getId().equals(food.getId()));
    }
 
    @Test
    void findByCategoryId_returnsEmpty_whenNoBudgetsForCategory() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));
 
        List<Budget> results = budgetRepository.findByCategoryId(rent.getId());
 
        assertThat(results).isEmpty();
    }
 
    @Test
    void findByCategoryId_returnsEmpty_whenCategoryDoesNotExist() {
        List<Budget> results = budgetRepository.findByCategoryId(999L);
 
        assertThat(results).isEmpty();
    }
 
    // ── Unique constraint: (categoryId, month) ────────────────────────────────
 
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // runs outside the test transaction so flush actually commits then rolls back on its own
    void save_throwsException_whenDuplicateCategoryAndMonth() {
        // Insert first budget directly — outside the test transaction so it's visible to the second insert
        Long foodId = food.getId();
        budgetRepository.saveAndFlush(Budget.builder()
                .category(food)
                .month(YearMonth.of(2026, 6))   // JUN_2026 — unique to this test
                .amount(new BigDecimal("500.00"))
                .build());
 
        assertThatThrownBy(() -> {
            budgetRepository.saveAndFlush(Budget.builder()
                    .category(categoryRepository.getReferenceById(foodId))
                    .month(YearMonth.of(2026, 6))
                    .amount(new BigDecimal("999.00"))
                    .build());
        }).isInstanceOf(DataIntegrityViolationException.class);
 
        // Clean up manually since we're outside the test transaction
        budgetRepository.deleteAll(budgetRepository.findByMonth(YearMonth.of(2026, 6)));
    }
 
    @Test
    void save_allowsSameCategoryDifferentMonth() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));
        Budget may = savedBudget(food, MAY_2026, new BigDecimal("600.00"));
 
        assertThat(may.getId()).isNotNull();
    }
 
    @Test
    void save_allowsSameMonthDifferentCategory() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));
        Budget rentBudget = savedBudget(rent, APR_2026, new BigDecimal("1000.00"));
 
        assertThat(rentBudget.getId()).isNotNull();
    }
 
    // ── Update ────────────────────────────────────────────────────────────────
 
    @Test
    void save_updatesExistingBudget() {
        Budget saved = savedBudget(food, APR_2026, new BigDecimal("500.00"));
        saved.setAmount(new BigDecimal("750.00"));
        budgetRepository.save(saved);
 
        Budget updated = budgetRepository.findById(saved.getId()).orElseThrow();
 
        assertThat(updated.getAmount()).isEqualByComparingTo("750.00");
    }
 
    // ── Delete ────────────────────────────────────────────────────────────────
 
    @Test
    void deleteById_removesBudget() {
        Budget saved = savedBudget(food, APR_2026, new BigDecimal("500.00"));
 
        budgetRepository.deleteById(saved.getId());
 
        assertThat(budgetRepository.findById(saved.getId())).isEmpty();
    }
 
    @Test
    void deleteById_allowsReinsertAfterDeletion() {
        Budget saved = savedBudget(food, APR_2026, new BigDecimal("500.00"));
        budgetRepository.deleteById(saved.getId());
        budgetRepository.flush();

        Budget reinserted = savedBudget(food, APR_2026, new BigDecimal("600.00"));

        assertThat(reinserted.getId()).isNotNull();
    }

    // ── ExistsByMonthAndCategoryId ────────────────────────────────────────────

    @Test
    void existsByMonthAndCategoryId_returnsTrue_whenBudgetExists() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));

        Boolean exists = budgetRepository.existsByMonthAndCategoryId(APR_2026, food.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByMonthAndCategoryId_returnsFalse_whenMonthDoesNotMatch() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));

        Boolean exists = budgetRepository.existsByMonthAndCategoryId(MAY_2026, food.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsByMonthAndCategoryId_returnsFalse_whenCategoryDoesNotMatch() {
        savedBudget(food, APR_2026, new BigDecimal("500.00"));

        Boolean exists = budgetRepository.existsByMonthAndCategoryId(APR_2026, rent.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsByMonthAndCategoryId_returnsFalse_whenNoBudgetsExist() {
        Boolean exists = budgetRepository.existsByMonthAndCategoryId(APR_2026, food.getId());

        assertThat(exists).isFalse();
    }
}