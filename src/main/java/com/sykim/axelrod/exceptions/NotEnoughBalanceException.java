package com.sykim.axelrod.exceptions;

public class NotEnoughBalanceException extends Exception {
    public NotEnoughBalanceException(String msg) {
        super(msg);
    }
    NotEnoughBalanceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
