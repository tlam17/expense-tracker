package com.tylerlam.expensetracker.report.dto;

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
public class BudgetReportResponse {
    private YearMonth month;
    private List<BudgetReportCategoryRow> budgets;
}
