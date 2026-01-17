package com.example.ecom.exceptions;

import com.example.ecom.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Bắt đúng ResourceNotFoundException của project
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // IMPORTANT: Handle JPA/Hibernate validation errors (entity-level)
    // Without this, constraint violations fall through to Spring Security and
    // return 401
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException e) {
        Map<String, String> response = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            response.put(fieldName, message);
        });
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle TransactionSystemException (wraps ConstraintViolationException from
    // JPA save)
    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<APIResponse> handleTransactionException(
            org.springframework.transaction.TransactionSystemException e) {
        Throwable cause = e.getRootCause();
        if (cause instanceof jakarta.validation.ConstraintViolationException cve) {
            StringBuilder message = new StringBuilder("Validation failed: ");
            cve.getConstraintViolations()
                    .forEach(v -> message.append(v.getPropertyPath()).append(" ").append(v.getMessage()).append("; "));
            return new ResponseEntity<>(new APIResponse(message.toString(), false), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new APIResponse("Transaction error: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle DataIntegrityViolationException (foreign key constraint, duplicate
    // key, etc.)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException e) {
        String message;
        if (e.getMessage() != null && e.getMessage().contains("foreign key constraint")) {
            message = "Cannot delete: This item is referenced by other records (orders, carts, etc.). Remove related records first.";
        } else if (e.getMessage() != null && e.getMessage().contains("constraint")) {
            message = "Cannot complete operation: Data integrity constraint violated. The item may be in use elsewhere.";
        } else {
            message = "Cannot complete operation: "
                    + (e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage());
        }
        return new ResponseEntity<>(new APIResponse(message, false), HttpStatus.CONFLICT);
    }

    // Catch-all for unhandled exceptions (return 500 instead of 401)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleGenericException(Exception e) {
        String message = "Internal server error: "
                + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
