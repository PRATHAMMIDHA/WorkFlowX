package com.workflowx.workspace.dto;

import com.workflowx.workspace.entity.Workspace;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class WorkspaceDTO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private int memberCount;
    private Instant createdAt;

    public static WorkspaceDTO from(Workspace workspace) {
        return WorkspaceDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerId(workspace.getOwner().getId())
                .ownerName(workspace.getOwner().getName())
                .memberCount(workspace.getMembers().size())
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}
