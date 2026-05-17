package com.tylerlam.expensetracker.expense;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.data.jpa.domain.Specification;

// This class contains static methods that return JPA Specifications for filtering expenses based on various criteria.
public class ExpenseSpecifications {
    
    public static Specification<Expense> hasMonth(YearMonth month) {
        return (root, query, criteriaBuilder) -> {
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            return criteriaBuilder.between(root.get("date"), startOfMonth, endOfMonth);
        };
    }

    public static Specification<Expense> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }
}
