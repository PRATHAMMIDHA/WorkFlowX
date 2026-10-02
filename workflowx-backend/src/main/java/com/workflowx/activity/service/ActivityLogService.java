package com.workflowx.activity.service;

import com.workflowx.activity.dto.ActivityLogDTO;
import com.workflowx.activity.entity.ActivityLog;
import com.workflowx.activity.repository.ActivityLogRepository;
import com.workflowx.common.enums.ActivityType;
import com.workflowx.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * ActivityLogService — records immutable audit log entries.
 *
 * WHY @Async? Activity logs should never slow down the main request.
 * They run in a separate thread pool, decoupled from the main transaction.
 *
 * WHY Propagation.REQUIRES_NEW? Ensures the log is committed in its own
 * transaction — even if the outer transaction rolls back, the log entry survives.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, ActivityType action, String entityType, Long entityId, String description) {
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .user(user)
                .build();
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getForEntity(String entityType, Long entityId, Pageable pageable) {
        return activityLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(ActivityLogDTO::from);
    }
}
