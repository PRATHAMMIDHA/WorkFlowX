package com.workflowx.project.dto;

import com.workflowx.project.entity.ProjectMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectMemberDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatarUrl;
    private String role;

    public static ProjectMemberDTO from(ProjectMember member) {
        return ProjectMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .userAvatarUrl(member.getUser().getAvatarUrl())
                .role(member.getRole().name())
                .build();
    }
}
