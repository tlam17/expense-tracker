package com.tylerlam.expensetracker.budget;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.data.jpa.domain.Specification;

// This class contains static methods that return JPA Specifications for filtering budgets based on various criteria.
public class BudgetSpecifications {

    public static Specification<Budget> hasMonth(YearMonth month) {
        return (root, query, criteriaBuilder) -> {
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            return criteriaBuilder.between(root.get("month"), startOfMonth, endOfMonth);
        };
    }

    public static Specification<Budget> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    };
}
