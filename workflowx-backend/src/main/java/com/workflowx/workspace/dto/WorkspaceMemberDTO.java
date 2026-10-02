package com.workflowx.workspace.dto;

import com.workflowx.workspace.entity.WorkspaceMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatarUrl;
    private String role;

    public static WorkspaceMemberDTO from(WorkspaceMember member) {
        return WorkspaceMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .userAvatarUrl(member.getUser().getAvatarUrl())
                .role(member.getRole().name())
                .build();
    }
}
