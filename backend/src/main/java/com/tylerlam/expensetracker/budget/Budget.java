package com.tylerlam.expensetracker.budget;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tylerlam.expensetracker.category.Category;
import com.tylerlam.expensetracker.shared.converter.YearMonthConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "budgets",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_budgets_category_month",
            columnNames = {"category_id", "month"}
        )
    }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Budget {
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "budgets_id_seq"
    )
    @SequenceGenerator(
        name = "budgets_id_seq",
        sequenceName = "budgets_id_seq",
        allocationSize = 1
    )
    @Column(
        updatable = false, 
        nullable = false
    )
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "category_id",
        nullable = false
    )
    private Category category;

    @Convert(converter = YearMonthConverter.class)
    @Column(
        nullable = false,
        length = 7
    )
    private YearMonth month;

    @Column(
        nullable = false, 
        precision = 19, 
        scale = 2
    )
    private BigDecimal amount;

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
