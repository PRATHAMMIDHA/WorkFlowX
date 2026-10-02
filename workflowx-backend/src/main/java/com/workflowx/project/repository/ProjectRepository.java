package com.workflowx.project.repository;

import com.workflowx.project.entity.Project;
import com.workflowx.workspace.entity.Workspace;
import com.workflowx.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByWorkspace(Workspace workspace);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.user = :user")
    List<Project> findAllByMember(User user);

    List<Project> findByWorkspaceAndCreatedBy(Workspace workspace, User createdBy);
}
