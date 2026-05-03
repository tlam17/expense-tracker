package com.tylerlam.expensetracker.category.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String name;

    @DecimalMin(value = "0.00", inclusive = false, message = "Default budget must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Default budget must be a valid monetary amount with up to 2 decimal places")
    private BigDecimal defaultBudget;
}
