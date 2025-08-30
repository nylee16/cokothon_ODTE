package com.odte.topicurator.auth.exception.exceptionhandler;

import com.odte.topicurator.auth.exception.AuthErrorCode;

public class ApiException extends RuntimeException {
    private final AuthErrorCode error;
    public ApiException(AuthErrorCode error){ super(error.code); this.error = error; }
    public AuthErrorCode getError(){ return error; }
}
