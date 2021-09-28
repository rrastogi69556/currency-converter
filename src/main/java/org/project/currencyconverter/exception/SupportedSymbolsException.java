package org.project.currencyconverter.exception;

import lombok.Getter;

@Getter
public class SupportedSymbolsException extends RuntimeException
{

    private static final long serialVersionUID = 1832533013860608970L;
    private int statusCode;

    public SupportedSymbolsException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

}
