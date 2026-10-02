package com.workflowx.task.repository;

import com.workflowx.task.entity.Task;
import com.workflowx.common.enums.TaskStatus;
import com.workflowx.project.entity.Project;
import com.workflowx.sprint.entity.Sprint;
import com.workflowx.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByProject(Project project);
    List<Task> findBySprint(Sprint sprint);
    List<Task> findByAssignee(User assignee);
    long countByProject(Project project);
    long countByProjectAndStatus(Project project, TaskStatus status);
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.dueDate < :today AND t.status <> :doneStatus")
    long countOverdueTasks(Project project, LocalDate today, TaskStatus doneStatus);
}
