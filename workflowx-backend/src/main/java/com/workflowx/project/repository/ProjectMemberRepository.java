package com.workflowx.project.repository;

import com.workflowx.project.entity.Project;
import com.workflowx.project.entity.ProjectMember;
import com.workflowx.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    boolean existsByProjectAndUser(Project project, User user);
    List<ProjectMember> findByProject(Project project);
    void deleteByProjectAndUser(Project project, User user);
}
