package org.project.currencyconverter.exception;

import lombok.Getter;

@Getter
public class CurrencyConversionException extends RuntimeException
{
    private int statusCode;
    public CurrencyConversionException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
