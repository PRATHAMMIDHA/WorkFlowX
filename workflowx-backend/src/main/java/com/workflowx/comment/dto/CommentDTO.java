package com.workflowx.comment.dto;

import com.workflowx.comment.entity.Comment;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class CommentDTO {
    private Long id;
    private String content;
    private Long taskId;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public static CommentDTO from(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .authorAvatarUrl(comment.getAuthor().getAvatarUrl())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
