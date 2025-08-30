package com.odte.topicurator.auth.exception.exceptionhandler;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApi(ApiException e){
        var err = e.getError();
        return ResponseEntity.status(err.status)
                .body(Map.of("timestamp", Instant.now().toString(),
                        "code", err.code,
                        "message", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCred(BadCredentialsException e){
        return ResponseEntity.status(401)
                .body(Map.of("timestamp", Instant.now().toString(),
                        "code", "BAD_CREDENTIALS",
                        "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception e){
        return ResponseEntity.internalServerError()
                .body(Map.of("timestamp", Instant.now().toString(),
                        "code", "SERVER_ERROR",
                        "message", e.getMessage()));
    }
}
