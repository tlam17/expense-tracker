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
}
