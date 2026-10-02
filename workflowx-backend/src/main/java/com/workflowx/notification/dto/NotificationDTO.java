package com.workflowx.notification.dto;

import com.workflowx.notification.entity.Notification;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String message;
    private String type;
    private boolean read;
    private Long entityId;
    private String entityType;
    private Instant createdAt;

    public static NotificationDTO from(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .entityId(notification.getEntityId())
                .entityType(notification.getEntityType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
