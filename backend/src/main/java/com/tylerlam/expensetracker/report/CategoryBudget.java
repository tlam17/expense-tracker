package com.tylerlam.expensetracker.report;

import java.math.BigDecimal;

// Record class to represent the budget in a specific category
public record CategoryBudget(String category, BigDecimal amount, BigDecimal spent) {}
