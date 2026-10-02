package com.workflowx.sprint.entity;

import com.workflowx.common.Auditable;
import com.workflowx.common.enums.SprintStatus;
import com.workflowx.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sprints", indexes = {
        @Index(name = "idx_sprints_project", columnList = "project_id")
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sprint extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String goal;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SprintStatus status = SprintStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
