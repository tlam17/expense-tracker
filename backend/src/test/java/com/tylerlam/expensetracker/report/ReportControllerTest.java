package com.tylerlam.expensetracker.report;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tylerlam.expensetracker.report.dto.MonthlyReportCategoryRow;
import com.tylerlam.expensetracker.report.dto.MonthlyReportResponse;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    private static final YearMonth MAY_2026 = YearMonth.of(2026, 5);

    // --- GET /api/reports/monthly ---

    @Test
    public void getMonthlyReport_returns200WithReport() throws Exception {
        List<MonthlyReportCategoryRow> byCategory = List.of(
                MonthlyReportCategoryRow.builder().category("Food").spent(new BigDecimal("300.00")).build(),
                MonthlyReportCategoryRow.builder().category("Transport").spent(new BigDecimal("150.00")).build()
        );
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(MAY_2026)
                .totalSpent(new BigDecimal("450.00"))
                .byCategory(byCategory)
                .build();
        when(reportService.getMonthlyReport(eq(MAY_2026))).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly").param("month", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-05"))
                .andExpect(jsonPath("$.totalSpent").value(450.00))
                .andExpect(jsonPath("$.byCategory.length()").value(2))
                .andExpect(jsonPath("$.byCategory[0].category").value("Food"))
                .andExpect(jsonPath("$.byCategory[0].spent").value(300.00))
                .andExpect(jsonPath("$.byCategory[1].category").value("Transport"))
                .andExpect(jsonPath("$.byCategory[1].spent").value(150.00));
    }

    @Test
    public void getMonthlyReport_returns200WithEmptyReport() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(MAY_2026)
                .totalSpent(BigDecimal.ZERO)
                .byCategory(List.of())
                .build();
        when(reportService.getMonthlyReport(eq(MAY_2026))).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly").param("month", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-05"))
                .andExpect(jsonPath("$.totalSpent").value(0))
                .andExpect(jsonPath("$.byCategory.length()").value(0));
    }

    @Test
    public void getMonthlyReport_returns400WhenMonthIsInvalid() throws Exception {
        mockMvc.perform(get("/api/reports/monthly").param("month", "not-a-month"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }
}
