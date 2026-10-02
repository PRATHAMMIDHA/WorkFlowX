package com.workflowx.workspace.repository;

import com.workflowx.workspace.entity.Workspace;
import com.workflowx.workspace.entity.WorkspaceMember;
import com.workflowx.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);
    boolean existsByWorkspaceAndUser(Workspace workspace, User user);
    List<WorkspaceMember> findByWorkspace(Workspace workspace);
}
