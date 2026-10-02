package com.workflowx.exception;

import com.workflowx.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — Centralized exception handling for the entire application.
 *
 * HOW IT WORKS:
 *
 * @RestControllerAdvice is a combination of:
 *   @ControllerAdvice → applies to all @Controller and @RestController classes
 *   @ResponseBody     → serializes return values to JSON automatically
 *
 * When ANY exception propagates out of a controller, Spring's
 * DispatcherServlet checks if this class has a matching @ExceptionHandler method.
 * If yes, that method handles the response — the client NEVER sees a raw stack trace.
 *
 * WHY centralize exception handling?
 *
 * Without this:
 *   - Each controller catches exceptions manually → massive duplication
 *   - Inconsistent error response shapes across endpoints
 *   - Stack traces potentially leaking to clients (security risk)
 *   - Hard to change error format globally
 *
 * With this:
 *   - Services throw domain exceptions (ResourceNotFoundException, etc.)
 *   - Controllers are clean — no try/catch
 *   - All errors return the same JSON structure
 *   - Error format can be changed in one place
 *
 * EXCEPTION HIERARCHY:
 *   Exception (all)
 *     ├── ResourceNotFoundException  → 404
 *     ├── BadRequestException        → 400
 *     ├── UnauthorizedException      → 403
 *     ├── ConflictException          → 409
 *     ├── MethodArgumentNotValidException (Bean Validation) → 400
 *     ├── HttpMessageNotReadableException (malformed JSON) → 400
 *     ├── MethodArgumentTypeMismatchException (wrong path var type) → 400
 *     └── Exception (catch-all)     → 500
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ──────────────────────────────────────────────────────────────────────
    // DOMAIN EXCEPTIONS
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Handles ResourceNotFoundException → HTTP 404 Not Found.
     * Thrown when: GET /api/tasks/999 but task 999 doesn't exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request));
    }

    /**
     * Handles BadRequestException → HTTP 400 Bad Request.
     * Thrown when: business rules are violated (e.g., sprint end < start date).
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        log.warn("Bad request: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request));
    }

    /**
     * Handles UnauthorizedException → HTTP 403 Forbidden.
     * Thrown when: an authenticated user tries to do something outside their permissions.
     *
     * NOTE: HTTP 401 (not authenticated) is handled by Spring Security's
     * AuthenticationEntryPoint — that will be wired up in Phase 3.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        log.warn("Authorization denied: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request));
    }

    /**
     * Handles ConflictException → HTTP 409 Conflict.
     * Thrown when: trying to register with an existing email, duplicate resources.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        log.warn("Conflict: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request));
    }

    // ──────────────────────────────────────────────────────────────────────
    // SPRING MVC / VALIDATION EXCEPTIONS
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Handles @Valid / @Validated violations → HTTP 400 Bad Request.
     *
     * Thrown when a @RequestBody fails Bean Validation constraints like:
     *   @NotBlank, @Email, @Size, @Min, @Max
     *
     * Example request body:
     *   { "email": "not-an-email", "password": "abc" }
     *
     * Example response:
     * {
     *   "status": 400,
     *   "error": "VALIDATION_ERROR",
     *   "message": "Validation failed",
     *   "validationErrors": {
     *     "email": "must be a valid email address",
     *     "password": "must be at least 8 characters"
     *   }
     * }
     *
     * We return field-level errors so the frontend can highlight
     * the exact form field that failed validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed: {} | Path: {}", validationErrors, request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles malformed JSON in request body → HTTP 400 Bad Request.
     *
     * Example: client sends "{ name: missing quotes }" (invalid JSON).
     * Without this handler, Spring returns a cryptic 400 with no useful message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Malformed request body | Path: {}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "MALFORMED_REQUEST",
                        "Request body is malformed or contains invalid JSON",
                        request));
    }

    /**
     * Handles type mismatch in path variables or request params → HTTP 400.
     *
     * Example: GET /api/tasks/abc where taskId expects a Long.
     * Without this, Spring returns a 500 with an internal error message.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format("Parameter '%s' has invalid value: '%s'",
                ex.getName(), ex.getValue());

        log.warn("Type mismatch: {} | Path: {}", message, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, request));
    }

    // ──────────────────────────────────────────────────────────────────────
    // CATCH-ALL — NEVER let raw errors leak to the client
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Catch-all handler for any unhandled exception → HTTP 500.
     *
     * WHY important?
     * Without this, Spring Boot returns its default "white label error page"
     * or leaks stack traces to the client — both are bad in production.
     *
     * We log the full exception for debugging but return a generic message
     * to the client (never expose internal details).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex,
            HttpServletRequest request) {

        // Log full stack trace for internal debugging
        log.error("Unhandled exception at path {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        request));
    }

    // ──────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ──────────────────────────────────────────────────────────────────────

    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message,
                                              HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}
