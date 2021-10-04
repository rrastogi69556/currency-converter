package org.project.currencyconverter.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        return getApiErrorResponseEntity(exception.getStatusCode(), exception.getMessage());

    }

    @ExceptionHandler(CurrencyConversionException.class)
    protected ResponseEntity<ApiResponse> handleException(CurrencyConversionException exception)
    {
        log.error("CurrencyConversionException", exception);
        return getApiErrorResponseEntity(exception.getStatusCode(), exception.getMessage());

    }

    @ExceptionHandler(ExchangeRatesException.class)
    protected ResponseEntity<ApiResponse> handleException(ExchangeRatesException exception)
    {
        log.error("ExchangeRatesException", exception);
        return getApiErrorResponseEntity(exception.getStatusCode(), exception.getMessage());

    }

    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<ApiResponse> handleException(HttpClientErrorException exception) throws JsonProcessingException
    {
        log.error("HttpClientErrorException", exception);
        return getApiErrorResponseEntity(exception.getRawStatusCode(), exception.getLocalizedMessage());

    }

    @ExceptionHandler(JsonProcessingException.class)
    protected ResponseEntity<ApiResponse> handleException(JsonProcessingException exception)
    {
        log.error("JsonProcessingException", exception);
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());

    }

    @ExceptionHandler(DateTimeException.class)
    protected ResponseEntity<ApiResponse> handleException(DateTimeException exception)
    {
        log.error("DateTimeException", exception);
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getLocalizedMessage());

    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ApiResponse> handleException(IllegalArgumentException jpe) {
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR.value(), jpe.getMessage());

    }

    @ExceptionHandler(ParseException.class)
    protected ResponseEntity<ApiResponse> handleException(ParseException jpe) {
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR.value(), jpe.getMessage());

    }

    private ResponseEntity<ApiResponse> getApiErrorResponseEntity(int statusCode, String message)
    {
        ApiResponse exception = new ApiResponse(statusCode);
        exception.setMessage(message);
        log.error(message);
        log.debug(message, exception);
        return buildResponseEntity(exception);
    }


    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ApiResponse> handleException(NullPointerException jpe)
    {
        log.error("NullPointerException", jpe);
        return getApiErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR.value(), jpe.getMessage());

    }

    private ResponseEntity<ApiResponse> buildResponseEntity(ApiResponse apiResponse)
    {
        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(apiResponse.getStatusCode()));
    }

}
