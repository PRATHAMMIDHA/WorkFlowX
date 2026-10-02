package com.workflowx.team.repository;

import com.workflowx.team.entity.Team;
import com.workflowx.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByWorkspace(Workspace workspace);
}
