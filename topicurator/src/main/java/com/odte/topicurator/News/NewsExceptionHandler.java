package com.odte.topicurator.News;

import com.odte.topicurator.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@Slf4j
@RestControllerAdvice(basePackages = "com.odte.topicurator.News")
@Order(1)
public class NewsExceptionHandler {

    // 뉴스 존재하지 않음
    @ExceptionHandler(RuntimeException.class) // 필요하면 NewsNotFoundException으로 대체
    public ResponseEntity<ApiResponse<Void>> handleNewsNotFound(RuntimeException e, HttpServletRequest req) {
        String id = UUID.randomUUID().toString();
        log.warn("[{}] 뉴스 없음 - {}: {}", id, req.getRequestURI(), e.getMessage());

        ApiResponse<Void> body = ApiResponse.fail("NEWS.NOT_FOUND", e.getMessage());
        enrich(body.getError(), req, id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 일반적인 서버 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e, HttpServletRequest req) {
        String id = UUID.randomUUID().toString();
        log.error("[{}] 처리되지 않은 예외 발생 at {}: {}", id, req.getRequestURI(), e.toString(), e);

        ApiResponse<Void> body = ApiResponse.fail("COMMON.SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        enrich(body.getError(), req, id);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 공통 에러 정보 enrichment
    private void enrich(ApiResponse.Error error, HttpServletRequest req, String id) {
        if (error == null) return;
        error.errorId = id;
        error.path = req.getRequestURI();
    }
}
