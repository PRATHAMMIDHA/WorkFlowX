package com.workflowx.team.entity;

import com.workflowx.common.Auditable;
import com.workflowx.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_user", columnNames = {"team_id", "user_id"}))
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
