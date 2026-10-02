package com.workflowx.workspace.entity;

import com.workflowx.common.Auditable;
import com.workflowx.common.enums.WorkspaceRole;
import com.workflowx.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_workspace_user", columnNames = {"workspace_id", "user_id"}),
        indexes = {
                @Index(name = "idx_wm_workspace", columnList = "workspace_id"),
                @Index(name = "idx_wm_user", columnList = "user_id")
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;
}
