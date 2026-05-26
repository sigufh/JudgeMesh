package com.judgemesh.problem.web;

import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiResponse<Void>> status(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorCode code = status == HttpStatus.NOT_FOUND ? ErrorCode.PROBLEM_NOT_FOUND : ErrorCode.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.fail(code, ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> generic(Exception ex) {
        return ResponseEntity.internalServerError().body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
    }
}
