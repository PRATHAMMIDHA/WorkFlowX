package com.workflowx.project.service;

import com.workflowx.common.enums.ProjectRole;
import com.workflowx.common.enums.ProjectStatus;
import com.workflowx.common.enums.TaskStatus;
import com.workflowx.common.enums.WorkspaceRole;
import com.workflowx.exception.BadRequestException;
import com.workflowx.exception.ConflictException;
import com.workflowx.exception.ResourceNotFoundException;
import com.workflowx.exception.UnauthorizedException;
import com.workflowx.project.dto.*;
import com.workflowx.project.entity.Project;
import com.workflowx.project.entity.ProjectMember;
import com.workflowx.project.repository.ProjectMemberRepository;
import com.workflowx.project.repository.ProjectRepository;
import com.workflowx.task.repository.TaskRepository;
import com.workflowx.user.entity.User;
import com.workflowx.user.repository.UserRepository;
import com.workflowx.workspace.entity.Workspace;
import com.workflowx.workspace.entity.WorkspaceMember;
import com.workflowx.workspace.repository.WorkspaceMemberRepository;
import com.workflowx.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectDTO create(User currentUser, CreateProjectRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", request.getWorkspaceId()));

        verifyWorkspaceMembership(workspace, currentUser);

        ProjectStatus status;
        try { status = ProjectStatus.valueOf(request.getStatus()); }
        catch (Exception e) { status = ProjectStatus.PLANNING; }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(status)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .workspace(workspace)
                .createdBy(currentUser)
                .build();
        projectRepository.save(project);

        ProjectMember creatorMember = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(ProjectRole.MANAGER)
                .build();
        projectMemberRepository.save(creatorMember);
        project.getMembers().add(creatorMember);

        return ProjectDTO.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByWorkspace(Long workspaceId, User currentUser) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
        verifyWorkspaceMembership(workspace, currentUser);
        return projectRepository.findByWorkspace(workspace).stream().map(ProjectDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectDTO getById(Long id, User currentUser) {
        Project project = findAndVerifyMembership(id, currentUser);
        return ProjectDTO.from(project);
    }

    public ProjectDTO update(Long id, User currentUser, CreateProjectRequest request) {
        Project project = findAndVerifyManagerAccess(id, currentUser);
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            try { project.setStatus(ProjectStatus.valueOf(request.getStatus())); }
            catch (IllegalArgumentException ignored) {}
        }
        return ProjectDTO.from(projectRepository.save(project));
    }

    public void delete(Long id, User currentUser) {
        Project project = findAndVerifyManagerAccess(id, currentUser);
        projectRepository.delete(project);
    }

    public ProjectMemberDTO addMember(Long projectId, User currentUser, Long userId) {
        Project project = findAndVerifyManagerAccess(projectId, currentUser);
        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (projectMemberRepository.existsByProjectAndUser(project, newUser)) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project).user(newUser).role(ProjectRole.MEMBER).build();
        return ProjectMemberDTO.from(projectMemberRepository.save(member));
    }

    public void removeMember(Long projectId, Long userId, User currentUser) {
        Project project = findAndVerifyManagerAccess(projectId, currentUser);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (project.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Cannot remove the project creator");
        }
        projectMemberRepository.deleteByProjectAndUser(project, target);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberDTO> getMembers(Long projectId, User currentUser) {
        Project project = findAndVerifyMembership(projectId, currentUser);
        return projectMemberRepository.findByProject(project)
                .stream().map(ProjectMemberDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectStatsDTO getStats(Long projectId, User currentUser) {
        Project project = findAndVerifyMembership(projectId, currentUser);
        long total = taskRepository.countByProject(project);
        long completed = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);
        long active = taskRepository.countByProjectAndStatus(project, TaskStatus.IN_PROGRESS);
        long backlog = taskRepository.countByProjectAndStatus(project, TaskStatus.BACKLOG);
        long overdue = taskRepository.countOverdueTasks(project, LocalDate.now(), TaskStatus.DONE);
        double pct = total == 0 ? 0 : Math.round((double) completed / total * 1000.0) / 10.0;

        return ProjectStatsDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(total).completedTasks(completed)
                .activeTasks(active).backlogTasks(backlog)
                .overdueTasks(overdue).completionPercentage(pct)
                .build();
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void verifyWorkspaceMembership(Workspace workspace, User user) {
        if (!workspaceMemberRepository.existsByWorkspaceAndUser(workspace, user)) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
    }

    public Project findAndVerifyMembership(Long id, User user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new UnauthorizedException("You are not a member of this project");
        }
        return project;
    }

    private Project findAndVerifyManagerAccess(Long id, User user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectMemberRepository.findByProjectAndUser(project, user)
                .filter(m -> m.getRole() == ProjectRole.MANAGER)
                .orElseThrow(() -> new UnauthorizedException("Only project managers can perform this action"));
        return project;
    }
}
