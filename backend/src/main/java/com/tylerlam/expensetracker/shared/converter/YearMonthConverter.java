package com.tylerlam.expensetracker.shared.converter;

import java.time.YearMonth;

import jakarta.persistence.AttributeConverter;

// Converts YearMonth to String in the format "YYYY-MM" for database storage and vice versa.
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        return yearMonth != null ? yearMonth.toString() : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(String value) {
        return value != null ? YearMonth.parse(value) : null;
    }
    
} 