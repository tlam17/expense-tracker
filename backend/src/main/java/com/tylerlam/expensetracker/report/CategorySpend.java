package com.tylerlam.expensetracker.report;

import java.math.BigDecimal;

// Record class to represent the spending in a specific category
public record CategorySpend(String category, BigDecimal spent) {}
