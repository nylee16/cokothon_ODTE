package com.odte.topicurator.Comments;

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
@RestControllerAdvice(basePackages = "com.odte.topicurator.Comments")
@Order(1)
public class CommentExceptionHandler {

    // 댓글 권한 오류
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurity(SecurityException e, HttpServletRequest req) {
        String id = UUID.randomUUID().toString();
        log.warn("[{}] 댓글 권한 오류 발생 - {}", id, e.getMessage());

        ApiResponse<Void> body = ApiResponse.fail("COMMON.NO_AUTH", e.getMessage());
        enrich(body.getError(), req, id);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 댓글 존재하지 않음 (404) 또는 기타 IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFound(IllegalArgumentException e, HttpServletRequest req) {
        String id = UUID.randomUUID().toString();
        log.warn("[{}] 댓글 처리 오류 - {}", id, req.getRequestURI());

        ApiResponse<Void> body;
        if (e.getMessage() != null && e.getMessage().contains("존재하지 않는 댓글")) {
            body = ApiResponse.fail("COMMON.NOT_FOUND", e.getMessage());
            enrich(body.getError(), req, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        // 다른 IllegalArgumentException
        body = ApiResponse.fail("COMMON.BAD_REQUEST", e.getMessage());
        enrich(body.getError(), req, id);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 공통 에러 정보 enrichment
    private void enrich(ApiResponse.Error error, HttpServletRequest req, String id) {
        if (error == null) {
            error = new ApiResponse.Error("COMMON.UNKNOWN", "알 수 없는 오류가 발생했습니다.");
        }
        error.errorId = id;
        error.path = req.getRequestURI();
    }
}
