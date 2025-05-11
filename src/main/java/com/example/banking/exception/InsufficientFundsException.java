package com.example.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Insufficient funds")
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient funds");
    }

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientFundsException(Throwable cause) {
        super(cause);
    }

    public InsufficientFundsException(String message, Throwable cause,
                                      boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    // Дополнительные конструкторы для удобства
    public InsufficientFundsException(Long accountId, BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(String.format("Account %d has insufficient funds. Current balance: %.2f, required: %.2f",
                accountId, currentBalance, requiredAmount));
    }

    public InsufficientFundsException(String accountNumber, BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(String.format("Account %s has insufficient funds. Current balance: %.2f, required: %.2f",
                accountNumber, currentBalance, requiredAmount));
    }
}