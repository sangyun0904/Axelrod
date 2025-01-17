package com.sykim.axelrod.exceptions;

public class AccountDoseNotExistException extends Exception{

    AccountDoseNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountDoseNotExistException(String message) {
        super(message);
    }
}
