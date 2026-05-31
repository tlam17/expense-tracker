package com.tylerlam.expensetracker.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tylerlam.expensetracker.report.dto.MonthlyReportResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private static final YearMonth MAY_2026 = YearMonth.of(2026, 5);

    // --- getMonthlyReport ---

    @Test
    public void getMonthlyReport_returnsReport_withMultipleCategories() {
        List<CategorySpend> spends = List.of(
                new CategorySpend("Food", new BigDecimal("300.00")),
                new CategorySpend("Transport", new BigDecimal("150.00"))
        );
        when(reportRepository.getMonthlySpendByCategory(any(), any())).thenReturn(spends);

        MonthlyReportResponse result = reportService.getMonthlyReport(MAY_2026);

        assertThat(result.getMonth()).isEqualTo(MAY_2026);
        assertThat(result.getTotalSpent()).isEqualByComparingTo("450.00");
        assertThat(result.getByCategory()).hasSize(2);
        assertThat(result.getByCategory().get(0).getCategory()).isEqualTo("Food");
        assertThat(result.getByCategory().get(0).getSpent()).isEqualByComparingTo("300.00");
        assertThat(result.getByCategory().get(1).getCategory()).isEqualTo("Transport");
        assertThat(result.getByCategory().get(1).getSpent()).isEqualByComparingTo("150.00");
    }

    @Test
    public void getMonthlyReport_returnsZeroTotal_whenNoSpends() {
        when(reportRepository.getMonthlySpendByCategory(any(), any())).thenReturn(List.of());

        MonthlyReportResponse result = reportService.getMonthlyReport(MAY_2026);

        assertThat(result.getMonth()).isEqualTo(MAY_2026);
        assertThat(result.getTotalSpent()).isEqualByComparingTo("0");
        assertThat(result.getByCategory()).isEmpty();
    }

    @Test
    public void getMonthlyReport_passesCorrectDateRangeToRepository() {
        when(reportRepository.getMonthlySpendByCategory(any(), any())).thenReturn(List.of());

        reportService.getMonthlyReport(MAY_2026);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportRepository).getMonthlySpendByCategory(startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(2026, 5, 31));
    }

    @Test
    public void getMonthlyReport_sumsTotalAcrossAllCategories() {
        List<CategorySpend> spends = List.of(
                new CategorySpend("Food", new BigDecimal("100.50")),
                new CategorySpend("Rent", new BigDecimal("1000.00")),
                new CategorySpend("Transport", new BigDecimal("49.50"))
        );
        when(reportRepository.getMonthlySpendByCategory(any(), any())).thenReturn(spends);

        MonthlyReportResponse result = reportService.getMonthlyReport(MAY_2026);

        assertThat(result.getTotalSpent()).isEqualByComparingTo("1150.00");
        assertThat(result.getByCategory()).hasSize(3);
    }
}
