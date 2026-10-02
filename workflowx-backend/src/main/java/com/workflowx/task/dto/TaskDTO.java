package com.workflowx.task.dto;

import com.workflowx.task.entity.Task;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private Long projectId;
    private String projectName;
    private Long sprintId;
    private String sprintName;
    private Long assigneeId;
    private String assigneeName;
    private String assigneeAvatarUrl;
    private Long createdById;
    private String createdByName;
    private int commentCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskDTO from(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .sprintName(task.getSprint() != null ? task.getSprint().getName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .assigneeAvatarUrl(task.getAssignee() != null ? task.getAssignee().getAvatarUrl() : null)
                .createdById(task.getCreatedBy().getId())
                .createdByName(task.getCreatedBy().getName())
                .commentCount(task.getComments().size())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
