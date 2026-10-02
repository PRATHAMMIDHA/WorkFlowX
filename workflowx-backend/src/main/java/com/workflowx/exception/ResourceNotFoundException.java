package com.workflowx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException — thrown when a requested entity does not exist.
 *
 * Examples:
 *   - GET /api/tasks/999 → task 999 does not exist
 *   - GET /api/projects/5 → project 5 not found
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND) is NOT used here deliberately.
 * We let GlobalExceptionHandler map this to 404, keeping all HTTP status
 * decisions in one centralized place.
 *
 * Extends RuntimeException (unchecked) so callers don't need to declare
 * it in throws clauses — this is the modern Spring approach.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor for the common pattern:
     *   throw new ResourceNotFoundException("Task", "id", 42L)
     *   → "Task not found with id: 42"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
