package com.gebeya.bankAPI.Model.DTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class OperationResult<T> {

    private boolean success;
    private String message;
    private T data;


    public OperationResult(boolean success, String message, T data) {

        this.success = success;
        this.message = message;
        this.data = data;
    }
//    public static <T> OperationResult<T> success(String message, T data) {
//        return new OperationResult<>(true, message, data);
//    }
//
//    public static <T> OperationResult<T> failure(String message) {
//        return new OperationResult<>(false, message, null);
//    }


    public OperationResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OperationResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}