package com.workflowx.activity.controller;

import com.workflowx.activity.dto.ActivityLogDTO;
import com.workflowx.activity.service.ActivityLogService;
import com.workflowx.common.ApiResponse;
import com.workflowx.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogService activityLogService;

    /**
     * GET /api/activity?entityType=Task&entityId=42&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityLogDTO>>> getActivity(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                activityLogService.getForEntity(entityType, entityId, pageable), "Activity retrieved"));
    }
}
