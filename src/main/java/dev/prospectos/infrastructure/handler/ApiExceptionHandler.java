package dev.prospectos.infrastructure.handler;

import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.infrastructure.api.leads.LeadSearchController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized API validation error handling.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();
        String message = errors.isEmpty() ? "Validation failed" : String.join("; ", errors);
        if (LeadSearchController.class.equals(ex.getParameter().getContainingClass())) {
            LeadSearchResponse response = new LeadSearchResponse(
                LeadSearchStatus.FAILED,
                List.of(),
                UUID.randomUUID(),
                message
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() == null ? "Invalid request" : ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", message));
    }

    private String formatFieldError(FieldError error) {
        if (error == null) {
            return "Invalid request";
        }
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
