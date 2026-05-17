package com.tylerlam.expensetracker.budget;

import java.time.YearMonth;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByMonth(YearMonth month);
    List<Budget> findByCategoryId(Long categoryId);
    Boolean existsByMonthAndCategoryId(YearMonth month, Long categoryId);
}
