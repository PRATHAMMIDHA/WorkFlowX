package com.workflowx.exception;

/**
 * UnauthorizedException — thrown when a user tries to access a resource
 * they do not have permission to access (authorization failure).
 *
 * IMPORTANT distinction:
 *
 *   HTTP 401 UNAUTHORIZED → "You are not authenticated" (no valid JWT)
 *     → Handled by Spring Security's AuthenticationEntryPoint, NOT here.
 *
 *   HTTP 403 FORBIDDEN    → "You are authenticated but lack permission"
 *     → Thrown by our service layer when a user tries to do something
 *        outside their role/scope. THIS exception.
 *
 * Examples:
 *   - A DEVELOPER tries to delete a project (only PROJECT_MANAGER can)
 *   - User A tries to edit User B's comment
 *   - A user tries to access a workspace they're not a member of
 *
 * Maps to HTTP 403 Forbidden in GlobalExceptionHandler.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
