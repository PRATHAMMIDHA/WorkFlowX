package com.workflowx.task.controller;

import com.workflowx.common.ApiResponse;
import com.workflowx.task.dto.*;
import com.workflowx.task.service.TaskService;
import com.workflowx.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDTO>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.create(user, request), "Task created"));
    }

    /**
     * GET /api/tasks?projectId=1&sprintId=2&status=TODO&priority=HIGH
     *       &assigneeId=3&keyword=login&page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> getTasks(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sprintId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dueDateFrom,
            @RequestParam(required = false) String dueDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        TaskFilterRequest filter = new TaskFilterRequest();
        filter.setProjectId(projectId);
        filter.setSprintId(sprintId);
        filter.setAssigneeId(assigneeId);
        filter.setStatus(status);
        filter.setPriority(priority);
        filter.setKeyword(keyword);
        if (dueDateFrom != null) filter.setDueDateFrom(LocalDate.parse(dueDateFrom));
        if (dueDateTo != null) filter.setDueDateTo(LocalDate.parse(dueDateTo));

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(ApiResponse.success(
                taskService.getTasks(filter, pageable, user), "Tasks retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getById(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getById(id, user), "Task retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> update(
            @PathVariable Long id, @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.update(id, user, request), "Task updated"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskDTO>> updateStatus(
            @PathVariable Long id, @RequestParam String status,
            @AuthenticationPrincipal User user) {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setStatus(status);
        return ResponseEntity.ok(ApiResponse.success(taskService.update(id, user, req), "Task status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        taskService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.success("Task deleted"));
    }
}
