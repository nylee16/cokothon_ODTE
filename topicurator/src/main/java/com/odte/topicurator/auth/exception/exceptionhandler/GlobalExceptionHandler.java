package com.odte.topicurator.auth.exception.exceptionhandler;

import com.odte.topicurator.auth.exception.exceptionhandler.AuthErrorCode;
import com.odte.topicurator.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private void enrich(ApiResponse.Error err, HttpServletRequest req, String errorId){
        if (err == null) return;
        err.path = req.getRequestURI();
        err.errorId = errorId;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        var code = e.getError();
        var body = ApiResponse.fail(code.code, e.getMessage(), e.getDetails());
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCred(BadCredentialsException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        var code = AuthErrorCode.INVALID_CREDENTIALS;
        var body = ApiResponse.fail(code.code, code.message);
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        var details = Map.<String, Object>of(
                "fieldErrors",
                e.getBindingResult().getFieldErrors().stream()
                        .map(f -> Map.of(
                                "field", f.getField(),
                                "message", f.getDefaultMessage(),
                                "rejected", f.getRejectedValue()
                        ))
                        .collect(Collectors.toList())
        );

        var body = ApiResponse.fail("COMMON.VALIDATION_ERROR", "입력값을 확인해 주세요.", details);
        enrich(body.getError(), req, id);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        var body = ApiResponse.fail("COMMON.SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        enrich(body.getError(), req, id);
        return ResponseEntity.internalServerError().body(body);
    }
}
