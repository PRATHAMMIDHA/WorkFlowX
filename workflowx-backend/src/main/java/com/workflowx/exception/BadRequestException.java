package com.workflowx.exception;

/**
 * BadRequestException — thrown when the client sends an invalid request
 * that passes field-level validation but fails business rule validation.
 *
 * Examples:
 *   - Registering with an email that already exists
 *   - Setting sprint end date before start date
 *   - Trying to activate a sprint when one is already active
 *   - Changing password with an incorrect old password
 *
 * Field-level validation (@NotBlank, @Email, @Size) is handled by
 * Bean Validation and mapped in GlobalExceptionHandler separately.
 * This exception is for semantic / business-rule violations.
 *
 * Maps to HTTP 400 Bad Request in GlobalExceptionHandler.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
