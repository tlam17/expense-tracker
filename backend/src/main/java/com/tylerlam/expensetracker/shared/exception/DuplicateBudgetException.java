package com.tylerlam.expensetracker.shared.exception;

public class DuplicateBudgetException extends RuntimeException {
    public DuplicateBudgetException(String message) {
        super(message);
    }
}
