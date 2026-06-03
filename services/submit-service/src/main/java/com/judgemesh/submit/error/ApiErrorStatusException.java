package com.judgemesh.submit.error;

import com.judgemesh.api.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiErrorStatusException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode errorCode;

    public ApiErrorStatusException(HttpStatus status, ErrorCode errorCode, String detail) {
        super(detail);
        this.status = status;
        this.errorCode = errorCode;
    }
}
