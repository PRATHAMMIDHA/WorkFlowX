package com.workflowx.task.service;

import com.workflowx.activity.service.ActivityLogService;
import com.workflowx.common.enums.ActivityType;
import com.workflowx.common.enums.NotificationType;
import com.workflowx.common.enums.TaskPriority;
import com.workflowx.common.enums.TaskStatus;
import com.workflowx.exception.ResourceNotFoundException;
import com.workflowx.exception.UnauthorizedException;
import com.workflowx.notification.service.NotificationService;
import com.workflowx.project.entity.Project;
import com.workflowx.project.service.ProjectService;
import com.workflowx.sprint.entity.Sprint;
import com.workflowx.sprint.repository.SprintRepository;
import com.workflowx.task.dto.*;
import com.workflowx.task.entity.Task;
import com.workflowx.task.repository.TaskRepository;
import com.workflowx.task.specification.TaskSpecification;
import com.workflowx.user.entity.User;
import com.workflowx.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public TaskDTO create(User currentUser, CreateTaskRequest request) {
        Project project = projectService.findAndVerifyMembership(request.getProjectId(), currentUser);

        TaskStatus status = parseStatus(request.getStatus(), TaskStatus.BACKLOG);
        TaskPriority priority = parsePriority(request.getPriority(), TaskPriority.MEDIUM);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .priority(priority)
                .dueDate(request.getDueDate())
                .project(project)
                .createdBy(currentUser)
                .build();

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));
            task.setSprint(sprint);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        taskRepository.save(task);

        activityLogService.log(currentUser, ActivityType.TASK_CREATED, "Task", task.getId(),
                currentUser.getName() + " created task: " + task.getTitle());

        if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
            notificationService.create(task.getAssignee(), NotificationType.TASK_ASSIGNED,
                    currentUser.getName() + " assigned you to: " + task.getTitle(),
                    "Task", task.getId());
        }

        return TaskDTO.from(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasks(TaskFilterRequest filter, Pageable pageable, User currentUser) {
        if (filter.getProjectId() != null) {
            projectService.findAndVerifyMembership(filter.getProjectId(), currentUser);
        }
        return taskRepository.findAll(TaskSpecification.buildSpec(filter), pageable)
                .map(TaskDTO::from);
    }

    @Transactional(readOnly = true)
    public TaskDTO getById(Long id, User currentUser) {
        Task task = findTask(id);
        projectService.findAndVerifyMembership(task.getProject().getId(), currentUser);
        return TaskDTO.from(task);
    }

    public TaskDTO update(Long id, User currentUser, UpdateTaskRequest request) {
        Task task = findTask(id);
        projectService.findAndVerifyMembership(task.getProject().getId(), currentUser);

        String oldStatus = task.getStatus().name();

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getStatus() != null) {
            TaskStatus newStatus = parseStatus(request.getStatus(), task.getStatus());
            if (newStatus != task.getStatus()) {
                String desc = currentUser.getName() + " changed task status: " + oldStatus + " → " + newStatus.name();
                activityLogService.log(currentUser, ActivityType.TASK_STATUS_CHANGED, "Task", task.getId(), desc);
                if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
                    notificationService.create(task.getAssignee(), NotificationType.TASK_STATUS_CHANGED,
                            "Task '" + task.getTitle() + "' status changed to " + newStatus.name(),
                            "Task", task.getId());
                }
                task.setStatus(newStatus);
            }
        }

        if (request.getPriority() != null) {
            task.setPriority(parsePriority(request.getPriority(), task.getPriority()));
        }

        // Handle assignee change (use -1 to unassign)
        if (request.getAssigneeId() != null) {
            if (request.getAssigneeId() == -1L) {
                task.setAssignee(null);
            } else {
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
                if (task.getAssignee() == null || !task.getAssignee().getId().equals(assignee.getId())) {
                    task.setAssignee(assignee);
                    activityLogService.log(currentUser, ActivityType.TASK_ASSIGNED, "Task", task.getId(),
                            currentUser.getName() + " assigned task to " + assignee.getName());
                    if (!assignee.getId().equals(currentUser.getId())) {
                        notificationService.create(assignee, NotificationType.TASK_ASSIGNED,
                                currentUser.getName() + " assigned you to: " + task.getTitle(),
                                "Task", task.getId());
                    }
                }
            }
        }

        // Handle sprint change (use -1 to remove sprint)
        if (request.getSprintId() != null) {
            if (request.getSprintId() == -1L) {
                task.setSprint(null);
            } else {
                Sprint sprint = sprintRepository.findById(request.getSprintId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));
                task.setSprint(sprint);
            }
        }

        activityLogService.log(currentUser, ActivityType.TASK_UPDATED, "Task", task.getId(),
                currentUser.getName() + " updated task: " + task.getTitle());

        return TaskDTO.from(taskRepository.save(task));
    }

    public void delete(Long id, User currentUser) {
        Task task = findTask(id);
        projectService.findAndVerifyMembership(task.getProject().getId(), currentUser);
        if (!task.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the task creator can delete this task");
        }
        activityLogService.log(currentUser, ActivityType.TASK_DELETED, "Task", task.getId(),
                currentUser.getName() + " deleted task: " + task.getTitle());
        taskRepository.delete(task);
    }

    public Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }

    // ── Enum helpers ─────────────────────────────────────────────────

    private TaskStatus parseStatus(String value, TaskStatus defaultVal) {
        if (value == null) return defaultVal;
        try { return TaskStatus.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) { return defaultVal; }
    }

    private TaskPriority parsePriority(String value, TaskPriority defaultVal) {
        if (value == null) return defaultVal;
        try { return TaskPriority.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) { return defaultVal; }
    }
}
