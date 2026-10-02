package com.workflowx.task.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateTaskRequest {
    @Size(min = 2, max = 200)
    private String title;

    private String description;
    private String status;
    private String priority;
    private Long assigneeId;
    private Long sprintId;
    private LocalDate dueDate;
    // Set sprintId = -1 to remove sprint association
    // Set assigneeId = -1 to unassign
}
