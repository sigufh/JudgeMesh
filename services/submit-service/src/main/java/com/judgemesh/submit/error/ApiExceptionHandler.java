package com.judgemesh.submit.error;

import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.api.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity.status(mapStatus(code)).body(ApiResponse.fail(code, ex.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
    }

    private HttpStatus mapStatus(ErrorCode code) {
        return switch (code) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND,
                 CONTEST_NOT_FOUND,
                 USER_NOT_FOUND,
                 PROBLEM_NOT_FOUND,
                 SUBMIT_NOT_FOUND,
                 CONTEST_NOT_STARTED,
                 CONTEST_ENDED,
                 CONTEST_NOT_REGISTERED -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.OK;
        };
    }
}
