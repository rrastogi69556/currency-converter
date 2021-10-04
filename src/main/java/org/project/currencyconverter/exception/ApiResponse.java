package org.project.currencyconverter.exception;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * An holder of customized error codes.
 */
@Getter
@Setter
public class ApiResponse implements Serializable
{

    private static final long serialVersionUID = -9008234528570047907L;
    private int statusCode;
    private String message;



    public ApiResponse(int statusCode)
    {
        this.statusCode = statusCode;
    }


}
