package com.sykim.axelrod.exceptions;

public class NotAvailableTickerException extends Exception{
    public NotAvailableTickerException(String message, Throwable cause) {
        super(message, cause);
    }
    public NotAvailableTickerException(String message) {
        super(message);
    }
}
