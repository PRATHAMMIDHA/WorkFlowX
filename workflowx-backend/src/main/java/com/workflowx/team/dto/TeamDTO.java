package com.workflowx.team.dto;

import com.workflowx.team.entity.Team;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class TeamDTO {
    private Long id;
    private String name;
    private String description;
    private Long workspaceId;
    private String workspaceName;
    private int memberCount;
    private Instant createdAt;

    public static TeamDTO from(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .workspaceId(team.getWorkspace().getId())
                .workspaceName(team.getWorkspace().getName())
                .memberCount(team.getMembers().size())
                .createdAt(team.getCreatedAt())
                .build();
    }
}
