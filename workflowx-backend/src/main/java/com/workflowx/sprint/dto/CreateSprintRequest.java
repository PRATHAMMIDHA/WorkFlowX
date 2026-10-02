package com.workflowx.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateSprintRequest {
    @NotBlank(message = "Sprint name is required")
    private String name;

    private String goal;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private LocalDate startDate;
    private LocalDate endDate;
}
