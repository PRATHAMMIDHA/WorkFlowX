package com.workflowx.sprint.controller;

import com.workflowx.common.ApiResponse;
import com.workflowx.sprint.dto.*;
import com.workflowx.sprint.service.SprintService;
import com.workflowx.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<ApiResponse<SprintDTO>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateSprintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(sprintService.create(user, request), "Sprint created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SprintDTO>>> getByProject(
            @AuthenticationPrincipal User user,
            @RequestParam Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.getByProject(projectId, user), "Sprints retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintDTO>> getById(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.getById(id, user), "Sprint retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintDTO>> update(
            @PathVariable Long id, @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateSprintRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.update(id, user, request), "Sprint updated"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SprintDTO>> updateStatus(
            @PathVariable Long id, @RequestParam String status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.updateStatus(id, status, user), "Sprint status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        sprintService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.success("Sprint deleted"));
    }
}
