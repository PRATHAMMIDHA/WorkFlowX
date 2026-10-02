package com.workflowx.exception;

/**
 * ConflictException — thrown when a request conflicts with existing data.
 *
 * Examples:
 *   - Registering with an email that already exists
 *   - Creating a workspace with a duplicate name (if we enforce uniqueness)
 *   - Adding a user who is already a project member
 *
 * Maps to HTTP 409 Conflict in GlobalExceptionHandler.
 *
 * WHY separate from BadRequestException?
 * RFC 7231 defines 409 specifically for state conflicts — the request
 * itself is well-formed, but it conflicts with current server state.
 * 400 is for malformed or invalid requests.
 * Keeping them separate gives the frontend precise error handling ability.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
