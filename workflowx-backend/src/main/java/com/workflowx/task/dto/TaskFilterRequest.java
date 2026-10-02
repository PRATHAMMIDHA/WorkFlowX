package com.workflowx.task.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * TaskFilterRequest — encapsulates all optional filter parameters.
 * Used by TaskSpecification to build dynamic JPA Criteria queries.
 */
@Data
public class TaskFilterRequest {
    private Long projectId;
    private Long sprintId;
    private Long assigneeId;
    private String status;       // TaskStatus enum name
    private String priority;     // TaskPriority enum name
    private String keyword;      // searches title + description
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
}
