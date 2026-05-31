package com.tylerlam.expensetracker.report.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportResponse {
    private YearMonth month;
    private BigDecimal totalSpent;
    private List<MonthlyReportCategoryRow> byCategory;
}
