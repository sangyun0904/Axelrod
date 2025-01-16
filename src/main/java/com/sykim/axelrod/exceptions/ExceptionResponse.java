package com.sykim.axelrod.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionResponse {
    private int responseCode;
    private String message;
}
