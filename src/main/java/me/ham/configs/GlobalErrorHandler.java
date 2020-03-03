package me.ham.configs;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@ControllerAdvice
public class GlobalErrorHandler implements ResponseErrorHandler {
    @ExceptionHandler(RuntimeException.class)
    public Object exceptionHandler(RuntimeException e){
        System.out.println("GlobalErrorHandler is called :::::::::");
        e.getStackTrace();
        return null;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        System.out.println("###################hasError");
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        System.out.println(response.getStatusCode().toString());
        System.out.println("###################handleError");

    }
}
