package com.tylerlam.expense_tracker.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getCategories_returns200() throws Exception {
      mvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    public void getCategories_returnsExpectedCategories() throws Exception {
        mvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("Food"))
            .andExpect(jsonPath("$[1]").value("Rent"))
            .andExpect(jsonPath("$[2]").value("Utilities"));
    }
}
