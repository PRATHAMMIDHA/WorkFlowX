package com.workflowx.team.dto;

import com.workflowx.team.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatarUrl;

    public static TeamMemberDTO from(TeamMember member) {
        return TeamMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .userAvatarUrl(member.getUser().getAvatarUrl())
                .build();
    }
}
