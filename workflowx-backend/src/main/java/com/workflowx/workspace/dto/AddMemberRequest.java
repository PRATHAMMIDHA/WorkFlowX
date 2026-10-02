package com.workflowx.workspace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    private String role = "MEMBER"; // OWNER, ADMIN, MEMBER
}
