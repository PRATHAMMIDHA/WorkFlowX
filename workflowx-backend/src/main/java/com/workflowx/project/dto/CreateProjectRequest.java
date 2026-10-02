package com.workflowx.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;

    private LocalDate startDate;
    private LocalDate endDate;
    private String status = "PLANNING";
}
