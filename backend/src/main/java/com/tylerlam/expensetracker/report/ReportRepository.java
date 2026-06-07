package com.tylerlam.expensetracker.report;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final EntityManager entityManager;
    private static final String CATEGORY_SPEND = "com.tylerlam.expensetracker.report.CategorySpend";
    private static final String CATEGORY_BUDGET = "com.tylerlam.expensetracker.report.CategoryBudget";

    // Get the total amount spent for each category within a given date range
    public List<CategorySpend> getMonthlySpendByCategory(LocalDate start, LocalDate end) {
        return entityManager.createQuery(
            "SELECT new " + CATEGORY_SPEND + "(e.category.name, SUM(e.amount)) " +
            "FROM Expense e " +
            "WHERE e.date >= :start AND e.date <= :end " +
            "GROUP BY e.category.name", 
            CategorySpend.class)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList();
    }

    // Get the budget limit, spent amount, and remaining amount for each category within a given date range
    public List<CategoryBudget> getBudgetReportByCategory(LocalDate start, LocalDate end) {
        return entityManager.createQuery(
            "SELECT new " + CATEGORY_BUDGET + "(e.category.name, b.amount, SUM(e.amount)) " +
            "FROM Expense e " +
            "LEFT JOIN Budget b ON e.category = b.category " +
            "WHERE e.date >= :start AND e.date <= :end " +
            "GROUP BY e.category.name, b.amount",
            CategoryBudget.class)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList();
    }
}
