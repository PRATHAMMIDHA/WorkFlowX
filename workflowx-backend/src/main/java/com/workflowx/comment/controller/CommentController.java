package com.workflowx.comment.controller;

import com.workflowx.comment.dto.CommentDTO;
import com.workflowx.comment.dto.CreateCommentRequest;
import com.workflowx.comment.service.CommentService;
import com.workflowx.common.ApiResponse;
import com.workflowx.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> create(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentService.create(taskId, user, request), "Comment added"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getByTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getByTask(taskId, user), "Comments retrieved"));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> update(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.update(commentId, user, request), "Comment updated"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {
        commentService.delete(commentId, user);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }
}
