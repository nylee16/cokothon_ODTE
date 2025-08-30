package com.odte.topicurator.auth.exception.exceptionhandler;

import com.odte.topicurator.auth.exception.exceptionhandler.AuthErrorCode;
import com.odte.topicurator.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.util.Map;
import java.util.Set;
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

    // --- 1) 우리 커스텀 예외 ---
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        var code = e.getError();
        log.warn("[{}] ApiException: {} ({})", id, e.getMessage(), code.code, e);
        var body = ApiResponse.fail(code.code, e.getMessage(), e.getDetails());
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    // --- 2) 인증 관련 표준 예외 ---
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCred(BadCredentialsException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] BadCredentials: {}", id, e.getMessage());
        var code = AuthErrorCode.INVALID_CREDENTIALS;
        var body = ApiResponse.fail(code.code, code.message);
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] AccessDenied: {}", id, e.getMessage());
        var code = AuthErrorCode.ACCESS_DENIED;
        var body = ApiResponse.fail(code.code, code.message);
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body);
    }

    // --- 3) JWT 예외 분기 ---
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwt(ExpiredJwtException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] JWT expired: {}", id, e.getMessage());
        var code = AuthErrorCode.TOKEN_EXPIRED;
        var body = ApiResponse.fail(code.code, code.message);
        enrich(body.getError(), req, id);
        return ResponseEntity.status(code.status).body(body); // 401
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwt(JwtException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] JWT invalid at {}: {}", id, req.getRequestURI(), e.getMessage());
        // refresh 엔드포인트면 리프레시 토큰 오류로 통일
        if (req.getRequestURI() != null && req.getRequestURI().contains("/api/auth/refresh")) {
            var code = AuthErrorCode.INVALID_REFRESH_TOKEN;
            var body = ApiResponse.fail(code.code, code.message, Map.of("reason", e.getMessage()));
            enrich(body.getError(), req, id);
            return ResponseEntity.status(code.status).body(body); // 401
        }
        // 그 외는 401로 AUTH.INVALID_TOKEN 문자열 코드 반환 (enum 없어도 사용 가능)
        var body = ApiResponse.fail("AUTH.INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.", Map.of("reason", e.getMessage()));
        enrich(body.getError(), req, id);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // --- 4) 요청 유효성/바인딩/파싱 예외 → 400 ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] Bean Validation failed: {} field errors", id, e.getBindingResult().getFieldErrorCount());
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

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] Bad Request at {}: {}", id, req.getRequestURI(), e.getMessage());
        var body = ApiResponse.fail("COMMON.BAD_REQUEST", "요청 파라미터가 올바르지 않습니다.", Map.of("reason", e.getMessage()));
        enrich(body.getError(), req, id);
        return ResponseEntity.badRequest().body(body);
    }

    // 잘못된 sort= 파라미터 (예: ["string"]) → 400
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Void>> handlePropertyRef(PropertyReferenceException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] Invalid sort property: {}", id, e.getMessage());
        var body = ApiResponse.fail("COMMON.INVALID_SORT", "정렬 필드를 확인해 주세요.", Map.of("reason", e.getMessage()));
        enrich(body.getError(), req, id);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.warn("[{}] Method Not Allowed: {}", id, e.getMessage());
        var body = ApiResponse.fail("COMMON.METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다.", Map.of("allowed", e.getSupportedHttpMethods()));
        enrich(body.getError(), req, id);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    // --- 5) 마지막 Fallback (진짜 알 수 없는 경우만 500) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e, HttpServletRequest req){
        var id = UUID.randomUUID().toString();
        log.error("[{}] 처리되지 않은 예외 발생 at {}: {}", id, req.getRequestURI(), e.toString(), e);
        var body = ApiResponse.fail("COMMON.SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        enrich(body.getError(), req, id);
        return ResponseEntity.internalServerError().body(body); // 500
    }
}
