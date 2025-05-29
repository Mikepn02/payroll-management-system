package com.mikepn.template.v1.handler;

import com.mikepn.template.v1.exceptions.OperationNotPermittedException;
import com.mikepn.template.v1.payload.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static com.mikepn.template.v1.enums.EBusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(LockedException e) {
        return ApiResponse.fail(
                ACCOUNT_LOCKED.getDescription(),
                UNAUTHORIZED,
                e.getMessage()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException e) {
        return ApiResponse.fail(
                ACCOUNT_DISABLED.getDescription(),
                UNAUTHORIZED,
                e.getMessage()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException() {
        return ApiResponse.fail(
                BAD_CREDENTIALS.getDescription(),
                UNAUTHORIZED,
                "Login and / or Password is incorrect"
        );
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ApiResponse<Object>> handleOperationNotPermittedException(OperationNotPermittedException e) {
        return ApiResponse.fail(
                "Operation not permitted",
                FORBIDDEN,
                e.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException exp) {
        Set<String> errors = new HashSet<>();
        exp.getBindingResult().getAllErrors().forEach(error -> {
            var errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });

        return ApiResponse.fail(
                "Validation failed",
                BAD_REQUEST,
                errors
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResponse.fail(
                UNAUTHORIZED_ACTION.getDescription(),
                FORBIDDEN,
                "You are not authorized to perform this action"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        e.printStackTrace();
        return ApiResponse.fail(
                "Internal server error, please contact the admin",
                INTERNAL_SERVER_ERROR,
                e.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        String userMessage = "Data integrity violation";

        if (rootMessage != null && rootMessage.contains("duplicate key value")) {
            if (rootMessage.contains("national_id")) {
                userMessage = "National ID already exists";
            } else if (rootMessage.contains("email")) {
                userMessage = "Email already exists";
            } else {
                userMessage = "Duplicate value violates unique constraint";
            }
            return ApiResponse.fail(
                    DUPLICATE_ENTRY.getDescription(),
                    CONFLICT,
                    userMessage
            );
        }

        return ApiResponse.fail(
                DATA_INTEGRITY_VIOLATION.getDescription(),
                CONFLICT,
                rootMessage
        );
    }


}
