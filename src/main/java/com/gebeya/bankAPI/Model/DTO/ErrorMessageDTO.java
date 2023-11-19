package com.gebeya.bankAPI.Model.DTO;

import org.springframework.http.HttpStatusCode;

import java.util.Date;

public class ErrorMessageDTO {
    private HttpStatusCode status;
    private String message;
    private Date timeStamp;

    public ErrorMessageDTO(){

    }
    public ErrorMessageDTO(HttpStatusCode status, String message)
    {
        this.status = status;
        this.message = message;
        this.timeStamp = new Date();
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public void setStatus(HttpStatusCode status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
