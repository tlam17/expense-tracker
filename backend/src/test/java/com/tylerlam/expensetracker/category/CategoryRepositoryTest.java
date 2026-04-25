package com.tylerlam.expensetracker.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
public class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CategoryRepository categoryRepository;

    // ── Helpers ──────────────────────────────────────────────────────────────
 
    private Category savedCategory(String name, BigDecimal defaultBudget) {
        return categoryRepository.saveAndFlush(
                Category.builder()
                        .name(name)
                        .defaultBudget(defaultBudget)
                        .build());
    }
 
    // ── Save ─────────────────────────────────────────────────────────────────
 
    @Test
    void save_persistsCategory() {
        Category saved = savedCategory("Food", new BigDecimal("400.00"));
 
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Food");
        assertThat(saved.getDefaultBudget()).isEqualByComparingTo("400.00");
    }
 
    @Test
    void save_setsCreatedAtAutomatically() {
        Category saved = savedCategory("Rent", null);
 
        assertThat(saved.getCreatedAt()).isNotNull();
    }
 
    @Test
    void save_allowsNullDefaultBudget() {
        Category saved = savedCategory("Transport", null);
 
        assertThat(saved.getDefaultBudget()).isNull();
    }
 
    // ── FindById ─────────────────────────────────────────────────────────────
 
    @Test
    void findById_returnsCategory_whenExists() {
        Category saved = savedCategory("Utilities", new BigDecimal("150.00"));
 
        Optional<Category> found = categoryRepository.findById(saved.getId());
 
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Utilities");
    }
 
    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<Category> found = categoryRepository.findById(999L);
 
        assertThat(found).isEmpty();
    }
 
    // ── FindAll ───────────────────────────────────────────────────────────────
 
    @Test
    void findAll_returnsAllSavedCategories() {
        savedCategory("Food", null);
        savedCategory("Rent", null);
        savedCategory("Transport", null);
 
        List<Category> all = categoryRepository.findAll();
 
        assertThat(all).hasSize(3);
    }
 
    @Test
    void findAll_returnsEmpty_whenNoneSaved() {
        List<Category> all = categoryRepository.findAll();
 
        assertThat(all).isEmpty();
    }
 
    // ── Update ────────────────────────────────────────────────────────────────
 
    @Test
    void save_updatesExistingCategory() {
        Category saved = savedCategory("Food", new BigDecimal("300.00"));
        saved.setName("Groceries");
        saved.setDefaultBudget(new BigDecimal("500.00"));
        categoryRepository.save(saved);
 
        Category updated = categoryRepository.findById(saved.getId()).orElseThrow();
 
        assertThat(updated.getName()).isEqualTo("Groceries");
        assertThat(updated.getDefaultBudget()).isEqualByComparingTo("500.00");
    }
 
    // ── Delete ────────────────────────────────────────────────────────────────
 
    @Test
    void deleteById_removesCategory() {
        Category saved = savedCategory("Entertainment", null);
 
        categoryRepository.deleteById(saved.getId());
 
        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }
 
    @Test
    void existsById_returnsTrue_whenExists() {
        Category saved = savedCategory("Health", null);
 
        assertThat(categoryRepository.existsById(saved.getId())).isTrue();
    }
 
    @Test
    void existsById_returnsFalse_whenNotExists() {
        assertThat(categoryRepository.existsById(999L)).isFalse();
    }
}