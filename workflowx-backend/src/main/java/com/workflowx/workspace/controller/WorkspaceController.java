package com.workflowx.workspace.controller;

import com.workflowx.common.ApiResponse;
import com.workflowx.user.entity.User;
import com.workflowx.workspace.dto.*;
import com.workflowx.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceDTO>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workspaceService.create(user, request), "Workspace created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceDTO>>> getMyWorkspaces(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getMyWorkspaces(user), "Workspaces retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getById(id, user), "Workspace retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceDTO>> update(
            @PathVariable Long id, @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.update(id, user, request), "Workspace updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        workspaceService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted"));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<WorkspaceMemberDTO>> addMember(
            @PathVariable Long id, @AuthenticationPrincipal User user,
            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workspaceService.addMember(id, user, request), "Member added"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        workspaceService.removeMember(id, userId, user);
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberDTO>>> getMembers(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getMembers(id, user), "Members retrieved"));
    }
}
