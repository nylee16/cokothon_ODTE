package com.odte.topicurator.auth.exception.exceptionhandler;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode {
    EMAIL_DUPLICATE      (HttpStatus.BAD_REQUEST,    "AUTH.EMAIL_DUPLICATE",       "이미 사용 중인 이메일입니다."),
    USERNAME_DUPLICATE   (HttpStatus.BAD_REQUEST,    "AUTH.USERNAME_DUPLICATE",    "이미 사용 중인 사용자명입니다."),
    USER_NOT_FOUND       (HttpStatus.NOT_FOUND,      "AUTH.USER_NOT_FOUND",        "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS  (HttpStatus.UNAUTHORIZED,   "AUTH.INVALID_CREDENTIALS",   "로그인에 실패하였습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,   "AUTH.INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_BLACKLISTED    (HttpStatus.UNAUTHORIZED,   "AUTH.TOKEN_BLACKLISTED",     "해당 토큰은 사용할 수 없습니다."),
    TOKEN_EXPIRED        (HttpStatus.UNAUTHORIZED,   "AUTH.TOKEN_EXPIRED",         "토큰이 만료되었습니다."),
    ACCESS_DENIED        (HttpStatus.FORBIDDEN,      "AUTH.ACCESS_DENIED",         "접근 권한이 없습니다.");

    public final HttpStatus status;
    public final String code;
    public final String message;
    AuthErrorCode(HttpStatus s, String c, String m){ this.status = s; this.code = c; this.message = m; }
}
