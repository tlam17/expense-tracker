package com.tylerlam.expensetracker.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tylerlam.expensetracker.category.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "expenses")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Expense {
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "expenses_id_seq"
    )
    @SequenceGenerator(
        name = "expenses_id_seq",
        sequenceName = "expenses_id_seq",
        allocationSize = 1
    )
    @Column(
        updatable = false, 
        nullable = false
    )
    private Long id;

    @Column(
        nullable = false, 
        precision = 19, 
        scale = 2
    )
    private BigDecimal amount;

    @Column(
        nullable = false
    )
    private LocalDate date;

    @Column(
        length = 255
    )
    private String description;

    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "category_id", 
        nullable = false
    )
    private Category category;

    @CreationTimestamp
    @Column(
        name = "created_at", 
        nullable = false, 
        updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
        name = "updated_at"
    )
    private Instant updatedAt;
}
