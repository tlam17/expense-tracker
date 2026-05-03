package com.tylerlam.expensetracker.category;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tylerlam.expensetracker.category.dto.CategoryRequest;
import com.tylerlam.expensetracker.category.dto.CategoryResponse;
import com.tylerlam.expensetracker.shared.exception.ResourceNotFoundException;

import static org.hamcrest.Matchers.nullValue;
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

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CategoryResponse buildResponse(Long id, String name, BigDecimal budget) {
        return CategoryResponse.builder()
                .id(id)
                .name(name)
                .defaultBudget(budget)
                .build();
    }

    private CategoryRequest buildRequest(String name, BigDecimal budget) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setDefaultBudget(budget);
        return request;
    }

    // --- GET /api/categories ---

    @Test
    public void getCategories_returns200WithCategories() throws Exception {
        List<CategoryResponse> categories = List.of(
                buildResponse(1L, "Food", new BigDecimal("500.00")),
                buildResponse(2L, "Rent", new BigDecimal("1500.00"))
        );
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[0].defaultBudget").value(500.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Rent"))
                .andExpect(jsonPath("$[1].defaultBudget").value(1500.00));
    }

    @Test
    public void getCategories_returns200WithEmptyList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/categories/{id} ---

    @Test
    public void getCategoryById_returns200WhenFound() throws Exception {
        CategoryResponse response = buildResponse(1L, "Food", new BigDecimal("500.00"));
        when(categoryService.getCategoryById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.defaultBudget").value(500.00));
    }

    @Test
    public void getCategoryById_returns404WhenNotFound() throws Exception {
        when(categoryService.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    public void getCategoryById_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/categories/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    // --- POST /api/categories ---

    @Test
    public void createCategory_returns201WithCreatedCategory() throws Exception {
        CategoryRequest request = buildRequest("Food", new BigDecimal("500.00"));
        CategoryResponse response = buildResponse(1L, "Food", new BigDecimal("500.00"));
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.defaultBudget").value(500.00));
    }

    @Test
    public void createCategory_returns201WithNullBudget() throws Exception {
        CategoryRequest request = buildRequest("Miscellaneous", null);
        CategoryResponse response = buildResponse(1L, "Miscellaneous", null);
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Miscellaneous"))
                .andExpect(jsonPath("$.defaultBudget").value(nullValue()));
    }

    @Test
    public void createCategory_returns400WhenNameIsBlank() throws Exception {
        CategoryRequest request = buildRequest("", new BigDecimal("500.00"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    public void createCategory_returns400WhenNameExceedsMaxLength() throws Exception {
        CategoryRequest request = buildRequest("A".repeat(101), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    public void createCategory_returns400WhenDefaultBudgetIsZero() throws Exception {
        CategoryRequest request = buildRequest("Food", BigDecimal.ZERO);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.defaultBudget").exists());
    }

    @Test
    public void createCategory_returns400WhenDefaultBudgetIsNegative() throws Exception {
        CategoryRequest request = buildRequest("Food", new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.defaultBudget").exists());
    }

    @Test
    public void createCategory_returns400WhenBodyIsInvalidJson() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request body"));
    }

    // --- PUT /api/categories/{id} ---

    @Test
    public void updateCategory_returns200WithUpdatedCategory() throws Exception {
        CategoryRequest request = buildRequest("Updated Food", new BigDecimal("600.00"));
        CategoryResponse response = buildResponse(1L, "Updated Food", new BigDecimal("600.00"));
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Food"))
                .andExpect(jsonPath("$.defaultBudget").value(600.00));
    }

    @Test
    public void updateCategory_returns404WhenNotFound() throws Exception {
        CategoryRequest request = buildRequest("Updated Food", new BigDecimal("600.00"));
        when(categoryService.updateCategory(eq(99L), any(CategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    public void updateCategory_returns400WhenNameIsBlank() throws Exception {
        CategoryRequest request = buildRequest("", new BigDecimal("600.00"));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    public void updateCategory_returns400WhenIdIsNotANumber() throws Exception {
        CategoryRequest request = buildRequest("Food", new BigDecimal("500.00"));

        mockMvc.perform(put("/api/categories/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    // --- DELETE /api/categories/{id} ---

    @Test
    public void deleteCategory_returns204WhenDeleted() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteCategory_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found with id: 99"))
                .when(categoryService).deleteCategory(99L);

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    public void deleteCategory_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(delete("/api/categories/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }
}
