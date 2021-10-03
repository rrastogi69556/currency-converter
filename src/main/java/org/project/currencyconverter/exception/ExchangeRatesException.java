package org.project.currencyconverter.exception;

import lombok.Getter;

@Getter
public class ExchangeRatesException extends RuntimeException
{
    private int statusCode;
    public ExchangeRatesException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
