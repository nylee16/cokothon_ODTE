package com.odte.topicurator.auth.exception.exceptionhandler;

import com.odte.topicurator.auth.exception.exceptionhandler.AuthErrorCode;
import com.odte.topicurator.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private void enrich(ApiResponse.Error err, HttpServletRequest req, String errorId){
        if (err == null) return;
        err.path = req.getRequestURI();
        err.errorId = errorId;
    }

    // 커스텀 API 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();

        // 로그 추가
        log.warn("[{}] ApiException 발생 - code: {}, message: {}", id, e.getError().code, e.getMessage(), e);

        var code = e.getError();
        var body = ApiResponse.fail(code.code, e.getMessage(), e.getDetails());
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    // 로그인 실패 (BadCredentials)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCred(BadCredentialsException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();

        // 로그 추가
        log.warn("[{}] 로그인 실패 - 이유: {}", id, e.getMessage());

        var code = AuthErrorCode.INVALID_CREDENTIALS;
        var body = ApiResponse.fail(code.code, code.message);
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    // Bean Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();

        // 로그 추가
        log.warn("[{}] 유효성 검사 실패 - {}건 오류", id, e.getBindingResult().getFieldErrorCount());

        Map<String, Object> details = Map.of(
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

    // 모든 예외 Fallback 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();

        // 로그 추가
        log.error("[{}] 처리되지 않은 예외 발생 at {}: {}", id, req.getRequestURI(), e.toString(), e);

        var body = ApiResponse.fail("COMMON.SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        enrich(body.getError(), req, id);
        return ResponseEntity.internalServerError().body(body);
    }
}
