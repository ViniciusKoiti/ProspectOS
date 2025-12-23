package dev.prospectos.web;

import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

/**
 * Centralized API validation error handling.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LeadSearchResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();
        String message = errors.isEmpty() ? "Validation failed" : String.join("; ", errors);

        LeadSearchResponse response = new LeadSearchResponse(
            LeadSearchStatus.FAILED,
            List.of(),
            UUID.randomUUID(),
            message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String formatFieldError(FieldError error) {
        if (error == null) {
            return "Invalid request";
        }
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
