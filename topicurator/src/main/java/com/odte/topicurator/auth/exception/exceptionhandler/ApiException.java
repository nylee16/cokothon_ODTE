package com.odte.topicurator.auth.exception.exceptionhandler;

import com.odte.topicurator.auth.exception.exceptionhandler.AuthErrorCode;
import java.util.Map;

public class ApiException extends RuntimeException {
    private final AuthErrorCode error;
    private final Map<String, Object> details; // optional

    public ApiException(AuthErrorCode error){ super(error.message); this.error = error; this.details = null; }
    public ApiException(AuthErrorCode error, String overrideMessage){ super(overrideMessage); this.error = error; this.details = null; }
    public ApiException(AuthErrorCode error, Map<String, Object> details){ super(error.message); this.error = error; this.details = details; }

    public AuthErrorCode getError(){ return error; }
    public Map<String, Object> getDetails(){ return details; }
}
