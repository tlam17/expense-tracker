package com.tylerlam.expensetracker.budget;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tylerlam.expensetracker.budget.dto.BudgetRequest;
import com.tylerlam.expensetracker.budget.dto.BudgetResponse;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.shared.exception.DuplicateBudgetException;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final YearMonth APR_2026 = YearMonth.of(2026, 4);
    private static final YearMonth MAY_2026 = YearMonth.of(2026, 5);

    private CategoryResponse buildCategoryResponse(Long id, String name, BigDecimal defaultBudget) {
        return CategoryResponse.builder()
                .id(id)
                .name(name)
                .defaultBudget(defaultBudget)
                .build();
    }

    private BudgetResponse buildResponse(Long id, BigDecimal amount, YearMonth month, CategoryResponse category) {
        return BudgetResponse.builder()
                .id(id)
                .amount(amount)
                .month(month)
                .category(category)
                .build();
    }

    private BudgetRequest buildRequest(BigDecimal amount, YearMonth month, Long categoryId) {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(amount);
        request.setMonth(month);
        request.setCategoryId(categoryId);
        return request;
    }

    // --- GET /api/budgets ---

    @Test
    public void getBudgets_returns200WithBudgets() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        List<BudgetResponse> budgets = List.of(
                buildResponse(1L, new BigDecimal("500.00"), APR_2026, category),
                buildResponse(2L, new BigDecimal("1000.00"), MAY_2026, category)
        );
        when(budgetService.getAllBudgets()).thenReturn(budgets);

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[0].month").value("2026-04"))
                .andExpect(jsonPath("$[0].category.id").value(1))
                .andExpect(jsonPath("$[0].category.name").value("Food"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(1000.00))
                .andExpect(jsonPath("$[1].month").value("2026-05"));
    }

    @Test
    public void getBudgets_returns200WithEmptyList() throws Exception {
        when(budgetService.getAllBudgets()).thenReturn(List.of());

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/budgets/{id} ---

    @Test
    public void getBudgetById_returns200WhenFound() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        BudgetResponse response = buildResponse(1L, new BigDecimal("500.00"), APR_2026, category);
        when(budgetService.getBudgetById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.month").value("2026-04"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("Food"))
                .andExpect(jsonPath("$.category.defaultBudget").value(500.00));
    }

    @Test
    public void getBudgetById_returns404WhenNotFound() throws Exception {
        when(budgetService.getBudgetById(99L))
                .thenThrow(new ResourceNotFoundException("Budget not found with id: 99"));

        mockMvc.perform(get("/api/budgets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Budget not found with id: 99"));
    }

    @Test
    public void getBudgetById_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/budgets/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    // --- POST /api/budgets ---

    @Test
    public void createBudget_returns201WithCreatedBudget() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, 1L);
        BudgetResponse response = buildResponse(1L, new BigDecimal("500.00"), APR_2026, category);
        when(budgetService.createBudget(any(BudgetRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.month").value("2026-04"))
                .andExpect(jsonPath("$.category.id").value(1));
    }

    @Test
    public void createBudget_returns409WhenDuplicateBudget() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, 1L);
        when(budgetService.createBudget(any(BudgetRequest.class)))
                .thenThrow(new DuplicateBudgetException("Budget already exists for month: 2026-04 and category id: 1"));

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Budget already exists for month: 2026-04 and category id: 1"));
    }

    @Test
    public void createBudget_returns404WhenCategoryNotFound() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, 99L);
        when(budgetService.createBudget(any(BudgetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    public void createBudget_returns400WhenMonthIsNull() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), null, 1L);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.month").exists());
    }

    @Test
    public void createBudget_returns400WhenAmountIsNull() throws Exception {
        BudgetRequest request = buildRequest(null, APR_2026, 1L);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    public void createBudget_returns400WhenAmountIsZero() throws Exception {
        BudgetRequest request = buildRequest(BigDecimal.ZERO, APR_2026, 1L);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    public void createBudget_returns400WhenCategoryIdIsNull() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("500.00"), APR_2026, null);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }

    @Test
    public void createBudget_returns400WhenBodyIsInvalidJson() throws Exception {
        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request body"));
    }

    // --- PUT /api/budgets/{id} ---

    @Test
    public void updateBudget_returns200WithUpdatedBudget() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), MAY_2026, 1L);
        BudgetResponse response = buildResponse(1L, new BigDecimal("750.00"), MAY_2026, category);
        when(budgetService.updateBudget(eq(1L), any(BudgetRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(750.00))
                .andExpect(jsonPath("$.month").value("2026-05"))
                .andExpect(jsonPath("$.category.id").value(1));
    }

    @Test
    public void updateBudget_returns404WhenNotFound() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 1L);
        when(budgetService.updateBudget(eq(99L), any(BudgetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Budget not found with id: 99"));

        mockMvc.perform(put("/api/budgets/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Budget not found with id: 99"));
    }

    @Test
    public void updateBudget_returns404WhenCategoryNotFound() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 99L);
        when(budgetService.updateBudget(eq(1L), any(BudgetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    public void updateBudget_returns409WhenDuplicateBudget() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), MAY_2026, 1L);
        when(budgetService.updateBudget(eq(1L), any(BudgetRequest.class)))
                .thenThrow(new DuplicateBudgetException("Budget already exists for month: 2026-05 and category id: 1"));

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Budget already exists for month: 2026-05 and category id: 1"));
    }

    @Test
    public void updateBudget_returns400WhenAmountIsNull() throws Exception {
        BudgetRequest request = buildRequest(null, APR_2026, 1L);

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    public void updateBudget_returns400WhenIdIsNotANumber() throws Exception {
        BudgetRequest request = buildRequest(new BigDecimal("750.00"), APR_2026, 1L);

        mockMvc.perform(put("/api/budgets/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    // --- DELETE /api/budgets/{id} ---

    @Test
    public void deleteBudget_returns204WhenDeleted() throws Exception {
        doNothing().when(budgetService).deleteBudget(1L);

        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteBudget_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Budget not found with id: 99"))
                .when(budgetService).deleteBudget(99L);

        mockMvc.perform(delete("/api/budgets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Budget not found with id: 99"));
    }

    @Test
    public void deleteBudget_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(delete("/api/budgets/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }
}
