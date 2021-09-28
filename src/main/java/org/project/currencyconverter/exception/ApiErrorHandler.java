package org.project.currencyconverter.exception;

import java.text.ParseException;
import java.time.DateTimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/*_________________________________________________
 | This class customizes & handles API error codes|
|_________________________________________________| */

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class ApiErrorHandler extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(SupportedSymbolsException.class)
    protected ResponseEntity<ApiResponse> handleException(SupportedSymbolsException exception)
    {
        log.error("SupportedSymbolsException", exception);
        return getApiErrorResponseEntity(HttpStatus.valueOf(exception.getStatusCode()), exception.getMessage());

    }

    @ExceptionHandler(CurrencyConversionException.class)
    protected ResponseEntity<ApiResponse> handleException(CurrencyConversionException exception)
    {
        log.error("CurrencyConversionException", exception);
        return getApiErrorResponseEntity(HttpStatus.valueOf(exception.getStatusCode()), exception.getMessage());

    }

    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<ApiResponse> handleException(HttpClientErrorException exception)
    {
        log.error("HttpClientErrorException", exception);
        return getApiErrorResponseEntity(HttpStatus.valueOf(String.valueOf(exception.getStatusCode())), exception.getLocalizedMessage());

    }

    @ExceptionHandler(DateTimeException.class)
    protected ResponseEntity<ApiResponse> handleException(DateTimeException exception)
    {
        log.error("DateTimeException", exception);
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, exception.getLocalizedMessage());

    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ApiResponse> handleException(IllegalArgumentException jpe) {
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, jpe.getMessage());

    }

    @ExceptionHandler(ParseException.class)
    protected ResponseEntity<ApiResponse> handleException(ParseException jpe) {
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, jpe.getMessage());

    }

    private ResponseEntity<ApiResponse> getApiErrorResponseEntity(HttpStatus notFound, String message)
    {
        ApiResponse exception = new ApiResponse(notFound);
        exception.setMessage(message);
        log.error(message);
        log.debug(message, exception);
        return buildResponseEntity(exception);
    }


    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ApiResponse> handleException(NullPointerException jpe)
    {
        log.error("NullPointerException", jpe);
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, jpe.getLocalizedMessage());

    }

    private ResponseEntity<ApiResponse> buildResponseEntity(ApiResponse apiResponse)
    {
        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

}
