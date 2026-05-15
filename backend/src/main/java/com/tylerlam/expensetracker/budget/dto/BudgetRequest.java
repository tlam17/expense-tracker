package com.tylerlam.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BudgetRequest {
    @NotNull(message = "Month is required and must be in the format YYYY-MM")
    private YearMonth month; // Format: "YYYY-MM"

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Amount must be a valid monetary amount with up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "A budget must be associated with a category")
    private Long categoryId;
}
