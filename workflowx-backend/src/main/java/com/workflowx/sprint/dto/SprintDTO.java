package com.workflowx.sprint.dto;

import com.workflowx.sprint.entity.Sprint;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class SprintDTO {
    private Long id;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long projectId;
    private String projectName;
    private Instant createdAt;

    public static SprintDTO from(Sprint sprint) {
        return SprintDTO.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus().name())
                .projectId(sprint.getProject().getId())
                .projectName(sprint.getProject().getName())
                .createdAt(sprint.getCreatedAt())
                .build();
    }
}
