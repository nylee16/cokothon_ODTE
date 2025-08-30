package com.odte.topicurator.auth.exception;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode {
    EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "EMAIL_DUPLICATE"),
    USERNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "USERNAME_DUPLICATE"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");

    public final HttpStatus status;
    public final String code;
    AuthErrorCode(HttpStatus s, String c){ this.status = s; this.code = c; }
}
