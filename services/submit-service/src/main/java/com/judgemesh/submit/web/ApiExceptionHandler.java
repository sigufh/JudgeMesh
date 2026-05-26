package com.judgemesh.submit.web;

import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiResponse<Void>> status(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorCode code = switch (status) {
            case NOT_FOUND -> ErrorCode.SUBMIT_NOT_FOUND;
            case TOO_MANY_REQUESTS -> ErrorCode.SUBMIT_RATE_LIMITED;
            case FORBIDDEN -> ErrorCode.CONTEST_NOT_REGISTERED;
            default -> ErrorCode.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.fail(code, ex.getReason()));
    }
}
