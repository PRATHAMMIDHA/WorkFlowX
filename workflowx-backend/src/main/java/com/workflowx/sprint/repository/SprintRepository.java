package com.workflowx.sprint.repository;

import com.workflowx.sprint.entity.Sprint;
import com.workflowx.common.enums.SprintStatus;
import com.workflowx.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProject(Project project);
    List<Sprint> findByProjectOrderByStartDateAsc(Project project);
    Optional<Sprint> findByProjectAndStatus(Project project, SprintStatus status);
    boolean existsByProjectAndStatus(Project project, SprintStatus status);
}
