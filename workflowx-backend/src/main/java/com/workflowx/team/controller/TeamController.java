package com.workflowx.team.controller;

import com.workflowx.common.ApiResponse;
import com.workflowx.team.dto.*;
import com.workflowx.team.service.TeamService;
import com.workflowx.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamDTO>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.create(user, request), "Team created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getByWorkspace(
            @AuthenticationPrincipal User user,
            @RequestParam Long workspaceId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getByWorkspace(workspaceId, user), "Teams retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamDTO>> getById(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getById(id, user), "Team retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamDTO>> update(
            @PathVariable Long id, @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(teamService.update(id, user, request), "Team updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        teamService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.success("Team deleted"));
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<TeamMemberDTO>> addMember(
            @PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.addMember(id, userId, user), "Member added"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        teamService.removeMember(id, userId, user);
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberDTO>>> getMembers(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getMembers(id, user), "Members retrieved"));
    }
}
