package com.tylerlam.expensetracker.category;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    @GetMapping("/api/categories")
    public List<String> getCategories() {
        return List.of("Food", "Rent", "Utilities");
    }

}
