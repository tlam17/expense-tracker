package com.tylerlam.expensetracker.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.expense.dto.ExpenseRequest;
import com.tylerlam.expensetracker.expense.dto.ExpenseResponse;
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

@WebMvcTest(ExpenseController.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private CategoryResponse buildCategoryResponse(Long id, String name, BigDecimal budget) {
        return CategoryResponse.builder()
                .id(id)
                .name(name)
                .defaultBudget(budget)
                .build();
    }

    private ExpenseResponse buildResponse(Long id, BigDecimal amount, LocalDate date, String description, CategoryResponse category) {
        return ExpenseResponse.builder()
                .id(id)
                .amount(amount)
                .date(date)
                .description(description)
                .category(category)
                .build();
    }

    private ExpenseRequest buildRequest(BigDecimal amount, LocalDate date, String description, Long categoryId) {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(amount);
        request.setDate(date);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        return request;
    }

    // --- GET /api/expenses ---

    @Test
    public void getExpenses_returns200WithExpenses() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        List<ExpenseResponse> expenses = List.of(
                buildResponse(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category),
                buildResponse(2L, new BigDecimal("100.00"), LocalDate.of(2024, 1, 11), "Groceries", category)
        );
        when(expenseService.getAllExpenses()).thenReturn(expenses);

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(25.00))
                .andExpect(jsonPath("$[0].description").value("Lunch"))
                .andExpect(jsonPath("$[0].category.id").value(1))
                .andExpect(jsonPath("$[0].category.name").value("Food"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(100.00));
    }

    @Test
    public void getExpenses_returns200WithEmptyList() throws Exception {
        when(expenseService.getAllExpenses()).thenReturn(List.of());

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/expenses/{id} ---

    @Test
    public void getExpenseById_returns200WhenFound() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        ExpenseResponse response = buildResponse(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        when(expenseService.getExpenseById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.description").value("Lunch"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("Food"))
                .andExpect(jsonPath("$.category.defaultBudget").value(500.00));
    }

    @Test
    public void getExpenseById_returns404WhenNotFound() throws Exception {
        when(expenseService.getExpenseById(99L))
                .thenThrow(new ResourceNotFoundException("Expense not found with id: 99"));

        mockMvc.perform(get("/api/expenses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
    }

    @Test
    public void getExpenseById_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/expenses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    // --- POST /api/expenses ---

    @Test
    public void createExpense_returns201WithCreatedExpense() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", 1L);
        ExpenseResponse response = buildResponse(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", category);
        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.description").value("Lunch"))
                .andExpect(jsonPath("$.category.id").value(1));
    }

    @Test
    public void createExpense_returns201WithNullDescription() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), null, 1L);
        ExpenseResponse response = buildResponse(1L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), null, category);
        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    public void createExpense_returns400WhenAmountIsNull() throws Exception {
        ExpenseRequest request = buildRequest(null, LocalDate.of(2024, 1, 10), "Lunch", 1L);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    public void createExpense_returns400WhenAmountIsZero() throws Exception {
        ExpenseRequest request = buildRequest(BigDecimal.ZERO, LocalDate.of(2024, 1, 10), "Lunch", 1L);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    public void createExpense_returns400WhenDateIsNull() throws Exception {
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), null, "Lunch", 1L);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.date").exists());
    }

    @Test
    public void createExpense_returns400WhenCategoryIdIsNull() throws Exception {
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "Lunch", null);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }

    @Test
    public void createExpense_returns400WhenDescriptionExceedsMaxLength() throws Exception {
        ExpenseRequest request = buildRequest(new BigDecimal("25.00"), LocalDate.of(2024, 1, 10), "A".repeat(256), 1L);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    public void createExpense_returns400WhenBodyIsInvalidJson() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request body"));
    }

    // --- PUT /api/expenses/{id} ---

    @Test
    public void updateExpense_returns200WithUpdatedExpense() throws Exception {
        CategoryResponse category = buildCategoryResponse(1L, "Food", new BigDecimal("500.00"));
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 1L);
        ExpenseResponse response = buildResponse(1L, new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", category);
        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.description").value("Dinner"))
                .andExpect(jsonPath("$.category.id").value(1));
    }

    @Test
    public void updateExpense_returns404WhenNotFound() throws Exception {
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 1L);
        when(expenseService.updateExpense(eq(99L), any(ExpenseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Expense not found with id: 99"));

        mockMvc.perform(put("/api/expenses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
    }

    @Test
    public void updateExpense_returns404WhenCategoryNotFound() throws Exception {
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 99L);
        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    public void updateExpense_returns400WhenAmountIsNull() throws Exception {
        ExpenseRequest request = buildRequest(null, LocalDate.of(2024, 1, 15), "Dinner", 1L);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    public void updateExpense_returns400WhenIdIsNotANumber() throws Exception {
        ExpenseRequest request = buildRequest(new BigDecimal("50.00"), LocalDate.of(2024, 1, 15), "Dinner", 1L);

        mockMvc.perform(put("/api/expenses/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    // --- DELETE /api/expenses/{id} ---

    @Test
    public void deleteExpense_returns204WhenDeleted() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L);

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteExpense_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Expense not found with id: 99"))
                .when(expenseService).deleteExpense(99L);

        mockMvc.perform(delete("/api/expenses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with id: 99"));
    }

    @Test
    public void deleteExpense_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(delete("/api/expenses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }
}
