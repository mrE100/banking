package com.example.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Transaction conflict occurred")
public class ConcurrentTransactionException extends RuntimeException {

    public ConcurrentTransactionException() {
        super();
    }

    public ConcurrentTransactionException(String message) {
        super(message);
    }

    public ConcurrentTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentTransactionException(Throwable cause) {
        super(cause);
    }

    protected ConcurrentTransactionException(String message, Throwable cause,
                                             boolean enableSuppression,
                                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}