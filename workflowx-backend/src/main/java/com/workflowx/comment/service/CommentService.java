package com.workflowx.comment.service;

import com.workflowx.activity.service.ActivityLogService;
import com.workflowx.comment.dto.CommentDTO;
import com.workflowx.comment.dto.CreateCommentRequest;
import com.workflowx.comment.entity.Comment;
import com.workflowx.comment.repository.CommentRepository;
import com.workflowx.common.enums.ActivityType;
import com.workflowx.common.enums.NotificationType;
import com.workflowx.exception.ResourceNotFoundException;
import com.workflowx.exception.UnauthorizedException;
import com.workflowx.notification.service.NotificationService;
import com.workflowx.project.service.ProjectService;
import com.workflowx.task.entity.Task;
import com.workflowx.task.service.TaskService;
import com.workflowx.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public CommentDTO create(Long taskId, User currentUser, CreateCommentRequest request) {
        Task task = taskService.findTask(taskId);
        projectService.findAndVerifyMembership(task.getProject().getId(), currentUser);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .author(currentUser)
                .build();
        commentRepository.save(comment);

        activityLogService.log(currentUser, ActivityType.COMMENT_ADDED, "Task", taskId,
                currentUser.getName() + " commented on task: " + task.getTitle());

        // Notify task assignee if different from commenter
        if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
            notificationService.create(task.getAssignee(), NotificationType.TASK_COMMENT_ADDED,
                    currentUser.getName() + " commented on: " + task.getTitle(),
                    "Task", taskId);
        }

        return CommentDTO.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getByTask(Long taskId, User currentUser) {
        Task task = taskService.findTask(taskId);
        projectService.findAndVerifyMembership(task.getProject().getId(), currentUser);
        return commentRepository.findByTaskOrderByCreatedAtAsc(task)
                .stream().map(CommentDTO::from).toList();
    }

    public CommentDTO update(Long commentId, User currentUser, CreateCommentRequest request) {
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only edit your own comments");
        }
        comment.setContent(request.getContent());
        activityLogService.log(currentUser, ActivityType.COMMENT_EDITED, "Task", comment.getTask().getId(),
                currentUser.getName() + " edited a comment");
        return CommentDTO.from(commentRepository.save(comment));
    }

    public void delete(Long commentId, User currentUser) {
        Comment comment = findComment(commentId);
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own comments");
        }
        activityLogService.log(currentUser, ActivityType.COMMENT_DELETED, "Task", comment.getTask().getId(),
                currentUser.getName() + " deleted a comment");
        commentRepository.delete(comment);
    }

    private Comment findComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }
}
