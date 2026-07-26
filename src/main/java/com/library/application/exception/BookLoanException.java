package com.library.application.exception;

public class BookLoanException extends RuntimeException {
    public BookLoanException(String message) {
        super(message);
    }
}
