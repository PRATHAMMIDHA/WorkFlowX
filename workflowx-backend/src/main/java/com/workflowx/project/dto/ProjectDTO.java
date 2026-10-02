package com.workflowx.project.dto;

import com.workflowx.project.entity.Project;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long workspaceId;
    private String workspaceName;
    private Long createdById;
    private String createdByName;
    private int memberCount;
    private Instant createdAt;

    public static ProjectDTO from(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .workspaceId(project.getWorkspace().getId())
                .workspaceName(project.getWorkspace().getName())
                .createdById(project.getCreatedBy().getId())
                .createdByName(project.getCreatedBy().getName())
                .memberCount(project.getMembers().size())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
