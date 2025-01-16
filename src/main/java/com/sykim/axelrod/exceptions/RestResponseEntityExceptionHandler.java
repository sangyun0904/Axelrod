package com.sykim.axelrod.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { NotAvailableTickerException.class })
    public ResponseEntity<ExceptionResponse> handleTickerException(NotAvailableTickerException exception) {
        System.out.println(Arrays.toString(exception.getStackTrace()));
        ExceptionResponse response = new ExceptionResponse(401, exception.getMessage());
        return ResponseEntity.ok(response);
    }
}
