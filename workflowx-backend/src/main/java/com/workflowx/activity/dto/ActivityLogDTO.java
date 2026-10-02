package com.workflowx.activity.dto;

import com.workflowx.activity.entity.ActivityLog;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ActivityLogDTO {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private Instant createdAt;

    public static ActivityLogDTO from(ActivityLog log) {
        return ActivityLogDTO.builder()
                .id(log.getId())
                .action(log.getAction().name())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getName() : "System")
                .userAvatarUrl(log.getUser() != null ? log.getUser().getAvatarUrl() : null)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
