package com.workflowx.project.controller;

import com.workflowx.common.ApiResponse;
import com.workflowx.project.dto.*;
import com.workflowx.project.service.ProjectService;
import com.workflowx.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.create(user, request), "Project created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getByWorkspace(
            @AuthenticationPrincipal User user,
            @RequestParam Long workspaceId) {
        return ResponseEntity.ok(ApiResponse.success(
                projectService.getProjectsByWorkspace(workspaceId, user), "Projects retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getById(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getById(id, user), "Project retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> update(
            @PathVariable Long id, @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectService.update(id, user, request), "Project updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        projectService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.success("Project deleted"));
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<ProjectMemberDTO>> addMember(
            @PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.addMember(id, user, userId), "Member added"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        projectService.removeMember(id, userId, user);
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<ProjectMemberDTO>>> getMembers(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getMembers(id, user), "Members retrieved"));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<ProjectStatsDTO>> getStats(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getStats(id, user), "Stats retrieved"));
    }
}
