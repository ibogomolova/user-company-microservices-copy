package com.companymicroservice.company.exception;

/**
 * Исключение, выбрасываемое, когда компания не найдена.
 */
public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
