package com.tylerlam.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExpenseRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Amount must be a valid monetary amount with up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Date of transaction is required")
    private LocalDate date;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @NotNull(message = "An expense must be associated with a category")
    private Long categoryId;
}
