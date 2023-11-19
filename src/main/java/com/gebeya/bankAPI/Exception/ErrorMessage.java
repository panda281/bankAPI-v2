package com.gebeya.bankAPI.Exception;

import org.springframework.http.HttpStatusCode;

import java.util.Date;

public class ErrorMessage extends RuntimeException {

    private static final long serialVersionUID = 1;

    private HttpStatusCode status;
    private String message;
    public ErrorMessage(HttpStatusCode status, String message)
    {
        super(message);
        this.status=status;
        this.message=message;
    }


    public HttpStatusCode getStatus() {
        return status;
    }

    public void setStatus(HttpStatusCode status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
