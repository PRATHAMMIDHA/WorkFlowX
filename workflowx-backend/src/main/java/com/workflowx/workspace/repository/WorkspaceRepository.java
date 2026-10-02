package com.workflowx.workspace.repository;

import com.workflowx.workspace.entity.Workspace;
import com.workflowx.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user = :user")
    List<Workspace> findAllByMember(User user);
}
