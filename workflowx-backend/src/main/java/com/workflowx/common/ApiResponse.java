package com.workflowx.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * ApiResponse<T> — Universal wrapper for ALL API responses in WorkFlowX.
 *
 * WHY use a response wrapper?
 *
 * Without a wrapper, the frontend receives inconsistent shapes:
 *   - Success: { "id": 1, "name": "Alpha" }
 *   - Error:   { "error": "Not found" }
 *
 * With a wrapper, every response has a predictable structure:
 *   - Success: { "success": true,  "data": {...}, "message": "..." }
 *   - Error:   { "success": false, "data": null,  "message": "...", "error": "NOT_FOUND" }
 *
 * This makes frontend API handling much simpler — one interceptor handles all cases.
 *
 * WHY use generics <T>?
 * The data payload type varies per endpoint:
 *   - /auth/login   → AuthResponse
 *   - /users/me     → UserDTO
 *   - /tasks        → Page<TaskDTO>
 * Generics let us type-check at compile time without duplicating this class.
 *
 * WHY @JsonInclude(NON_NULL)?
 * Fields with null values are excluded from JSON output.
 * So a success response won't include "error": null in the JSON body.
 *
 * WHY @Builder?
 * Lombok's @Builder generates the builder pattern, letting us write:
 *   ApiResponse.<UserDTO>builder()
 *     .success(true)
 *     .data(userDTO)
 *     .message("Profile retrieved")
 *     .build();
 * Instead of a verbose constructor call.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * true  → request succeeded
     * false → request failed
     */
    private boolean success;

    /**
     * ISO-8601 UTC timestamp of when this response was generated.
     * Useful for debugging and log correlation.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Human-readable message describing the result.
     * Examples: "User registered successfully", "Task not found"
     */
    private String message;

    /**
     * The actual payload.
     * null on error responses.
     */
    private T data;

    // ──────────────────────────────────────────────────────────
    // Static factory methods — convenience shortcuts so callers
    // don't have to repeat success(true) or success(false).
    // ──────────────────────────────────────────────────────────

    /**
     * Creates a successful response with data and a message.
     *
     * Usage:
     *   return ResponseEntity.ok(ApiResponse.success(userDTO, "Profile retrieved"));
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Creates a successful response with only a message (no data).
     * Useful for operations like DELETE that return 204 No Content,
     * or for simple confirmation responses.
     *
     * Usage:
     *   return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully"));
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Creates an error response with a message.
     * NOTE: Error responses are primarily built in GlobalExceptionHandler,
     * but this is kept here for flexibility.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
