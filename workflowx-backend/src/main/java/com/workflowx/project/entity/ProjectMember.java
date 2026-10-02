package com.workflowx.project.entity;

import com.workflowx.common.Auditable;
import com.workflowx.common.enums.ProjectRole;
import com.workflowx.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_user", columnNames = {"project_id", "user_id"}),
        indexes = {
                @Index(name = "idx_pm_project", columnList = "project_id"),
                @Index(name = "idx_pm_user", columnList = "user_id")
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProjectRole role = ProjectRole.MEMBER;
}
