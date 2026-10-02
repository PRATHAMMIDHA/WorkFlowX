package com.workflowx.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * ErrorResponse — The standard error JSON structure returned by GlobalExceptionHandler.
 *
 * Every error response looks like:
 * {
 *   "timestamp": "2026-10-01T17:31:00Z",
 *   "status": 404,
 *   "error": "NOT_FOUND",
 *   "message": "Task not found with id: 42",
 *   "path": "/api/tasks/42",
 *   "validationErrors": null   <-- only present for 400 validation errors
 * }
 *
 * Kept separate from ApiResponse<T> because:
 *   - Error responses have a different shape (no "data", no "success")
 *   - They have an additional "validationErrors" field for field-level errors
 *   - Mixing them would require awkward generics like ApiResponse<Map<String,String>>
 *
 * @JsonInclude(NON_NULL) ensures "validationErrors" is omitted from JSON
 * unless it's a validation error response.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Only populated for HTTP 400 VALIDATION_ERROR responses.
     * Key = field name, Value = validation message.
     *
     * Example:
     * {
     *   "email": "must be a valid email address",
     *   "password": "size must be between 8 and 100"
     * }
     */
    private Map<String, String> validationErrors;
}
