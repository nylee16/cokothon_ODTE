package com.odte.topicurator.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message; // 성공/실패 공통 메시지
    private T data;
    private Error error;

    // ===== Factory =====
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data = data;
        return r;
    }

    public static ApiResponse<Void> successWithNoData(String message) {
        ApiResponse<Void> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        return r;
    }

    public static ApiResponse<Void> fail(String message) {
        ApiResponse<Void> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.error = new Error(null, message);
        return r;
    }

    // (유지) 코드 + 메시지 버전도 지원 (기존 핸들러와 호환)
    public static ApiResponse<Void> fail(String code, String message) {
        ApiResponse<Void> r = fail(message);
        r.error.code = code;
        return r;
    }

    public static ApiResponse<Void> fail(String code, String message, Map<String, Object> details) {
        ApiResponse<Void> r = fail(code, message);
        r.error.details = details;
        return r;
    }

    // ===== Getters =====
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public Error getError() { return error; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Error {
        public final String timestamp = Instant.now().toString();
        public String code;              // ex) AUTH.INVALID_CREDENTIALS
        public String message;           // 사용자 메시지(ko)
        public String path;              // 요청 경로
        public String errorId;           // 서버 추적용
        public Map<String, Object> details; // 필드 에러 등

        public Error(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
