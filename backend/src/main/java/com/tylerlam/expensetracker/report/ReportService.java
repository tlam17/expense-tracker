package com.tylerlam.expensetracker.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tylerlam.expensetracker.report.dto.MonthlyReportCategoryRow;
import com.tylerlam.expensetracker.report.dto.MonthlyReportResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportRepository reportRepository;

    // Get the spending report for a specific month
    public MonthlyReportResponse getMonthlyReport(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<CategorySpend> categorySpends = reportRepository.getMonthlySpendByCategory(start, end);

        List<MonthlyReportCategoryRow> byCategory = categorySpends.stream()
                .map(this::toRow)
                .toList();
        
        BigDecimal totalSpent = categorySpends.stream()
                .map(CategorySpend::spent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MonthlyReportResponse.builder()
                .month(month)
                .totalSpent(totalSpent)
                .byCategory(byCategory)
                .build();
    }

    // Helper method to convert CategorySpend to MonthlyReportCategoryRow
    private MonthlyReportCategoryRow toRow(CategorySpend categorySpend) {
        return MonthlyReportCategoryRow.builder()
                .category(categorySpend.category())
                .spent(categorySpend.spent())
                .build();
    }
}
