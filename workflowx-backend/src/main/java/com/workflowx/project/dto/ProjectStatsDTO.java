package com.workflowx.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectStatsDTO {
    private Long projectId;
    private String projectName;
    private long totalTasks;
    private long completedTasks;
    private long activeTasks;
    private long backlogTasks;
    private long overdueTasks;
    private double completionPercentage;
}
