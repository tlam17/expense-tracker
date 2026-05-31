package com.tylerlam.expensetracker.report.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportCategoryRow {
    private String category;
    private BigDecimal spent;
}
