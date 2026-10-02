package com.workflowx.user.dto;

import com.workflowx.user.entity.User;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private String avatarUrl;
    private String role;
    private boolean active;
    private Instant createdAt;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
