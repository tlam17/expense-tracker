package com.tylerlam.expensetracker.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.category.CategoryRepository;
import com.tylerlam.expensetracker.expense.Expense;
import com.tylerlam.expensetracker.expense.ExpenseRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(ReportRepository.class)
public class ReportRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category food;
    private Category transport;

    private static final LocalDate MAY_1 = LocalDate.of(2026, 5, 1);
    private static final LocalDate MAY_31 = LocalDate.of(2026, 5, 31);

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        food = categoryRepository.save(Category.builder().name("Food").build());
        transport = categoryRepository.save(Category.builder().name("Transport").build());
    }

    private Expense savedExpense(BigDecimal amount, LocalDate date, Category category) {
        return expenseRepository.saveAndFlush(
                Expense.builder()
                        .amount(amount)
                        .date(date)
                        .category(category)
                        .description("test expense")
                        .build());
    }

    // ── getMonthlySpendByCategory ─────────────────────────────────────────────

    @Test
    void getMonthlySpendByCategory_returnsEmpty_whenNoExpenses() {
        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).isEmpty();
    }

    @Test
    void getMonthlySpendByCategory_returnsOneRow_perCategory() {
        savedExpense(new BigDecimal("100.00"), LocalDate.of(2026, 5, 10), food);
        savedExpense(new BigDecimal("50.00"), LocalDate.of(2026, 5, 15), transport);

        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategorySpend::category)
                .containsExactlyInAnyOrder("Food", "Transport");
    }

    @Test
    void getMonthlySpendByCategory_sumsMultipleExpensesForSameCategory() {
        savedExpense(new BigDecimal("100.00"), LocalDate.of(2026, 5, 5), food);
        savedExpense(new BigDecimal("200.00"), LocalDate.of(2026, 5, 10), food);
        savedExpense(new BigDecimal("50.00"), LocalDate.of(2026, 5, 20), food);

        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("Food");
        assertThat(result.get(0).spent()).isEqualByComparingTo("350.00");
    }

    @Test
    void getMonthlySpendByCategory_excludesExpensesBeforeStartDate() {
        savedExpense(new BigDecimal("500.00"), LocalDate.of(2026, 4, 30), food); // before range
        savedExpense(new BigDecimal("100.00"), LocalDate.of(2026, 5, 1), food);  // in range

        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).spent()).isEqualByComparingTo("100.00");
    }

    @Test
    void getMonthlySpendByCategory_excludesExpensesAfterEndDate() {
        savedExpense(new BigDecimal("100.00"), LocalDate.of(2026, 5, 31), food); // in range
        savedExpense(new BigDecimal("500.00"), LocalDate.of(2026, 6, 1), food);  // after range

        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).spent()).isEqualByComparingTo("100.00");
    }

    @Test
    void getMonthlySpendByCategory_includesBoundaryDates() {
        savedExpense(new BigDecimal("100.00"), MAY_1, food);
        savedExpense(new BigDecimal("200.00"), MAY_31, food);

        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).spent()).isEqualByComparingTo("300.00");
    }

    @Test
    void getMonthlySpendByCategory_returnsCorrectAmountsPerCategory() {
        savedExpense(new BigDecimal("300.00"), LocalDate.of(2026, 5, 10), food);
        savedExpense(new BigDecimal("150.00"), LocalDate.of(2026, 5, 12), transport);
        savedExpense(new BigDecimal("50.00"), LocalDate.of(2026, 5, 20), transport);

        List<CategorySpend> result = reportRepository.getMonthlySpendByCategory(MAY_1, MAY_31);

        assertThat(result).hasSize(2);
        CategorySpend foodSpend = result.stream().filter(s -> s.category().equals("Food")).findFirst().orElseThrow();
        CategorySpend transportSpend = result.stream().filter(s -> s.category().equals("Transport")).findFirst().orElseThrow();
        assertThat(foodSpend.spent()).isEqualByComparingTo("300.00");
        assertThat(transportSpend.spent()).isEqualByComparingTo("200.00");
    }
}
