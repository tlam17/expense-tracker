package com.tylerlam.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import com.tylerlam.expensetracker.category.dto.CategoryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {
    private Long id;
    private YearMonth month;
    private BigDecimal amount;
    private CategoryResponse category;
}
