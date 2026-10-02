package com.workflowx.notification.service;

import com.workflowx.common.enums.NotificationType;
import com.workflowx.notification.dto.NotificationDTO;
import com.workflowx.notification.entity.Notification;
import com.workflowx.notification.repository.NotificationRepository;
import com.workflowx.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Creates a notification asynchronously so it never blocks the main request.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(User recipient, NotificationType type, String message, String entityType, Long entityId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getMyNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(NotificationDTO::from);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }
}
