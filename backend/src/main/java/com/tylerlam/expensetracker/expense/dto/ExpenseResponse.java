package com.tylerlam.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tylerlam.expensetracker.category.dto.CategoryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String description;
    private LocalDate date;
    private BigDecimal amount; 
    private CategoryResponse category;
}
