package com.tylerlam.expensetracker.report;

import java.time.YearMonth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tylerlam.expensetracker.report.dto.BudgetReportResponse;
import com.tylerlam.expensetracker.report.dto.MonthlyReportResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/monthly?month=YYYY-MM - Get report for a specific month
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@RequestParam YearMonth month) {
        MonthlyReportResponse response = reportService.getMonthlyReport(month);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // GET /api/reports/budget?month=YYYY-MM - Get spent vs budget report for a specific month
    @GetMapping("/budget")
    public ResponseEntity<BudgetReportResponse> getBudgetReport(@RequestParam YearMonth month) {
        BudgetReportResponse response = reportService.getBudgetReport(month);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
