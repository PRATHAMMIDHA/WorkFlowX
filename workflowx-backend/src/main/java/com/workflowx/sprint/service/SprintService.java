package com.workflowx.sprint.service;

import com.workflowx.common.enums.SprintStatus;
import com.workflowx.exception.BadRequestException;
import com.workflowx.exception.ResourceNotFoundException;
import com.workflowx.project.entity.Project;
import com.workflowx.project.service.ProjectService;
import com.workflowx.sprint.dto.CreateSprintRequest;
import com.workflowx.sprint.dto.SprintDTO;
import com.workflowx.sprint.entity.Sprint;
import com.workflowx.sprint.repository.SprintRepository;
import com.workflowx.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectService projectService;

    public SprintDTO create(User currentUser, CreateSprintRequest request) {
        Project project = projectService.findAndVerifyMembership(request.getProjectId(), currentUser);

        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Sprint end date must be after start date");
        }

        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.PLANNED)
                .project(project)
                .build();
        return SprintDTO.from(sprintRepository.save(sprint));
    }

    @Transactional(readOnly = true)
    public List<SprintDTO> getByProject(Long projectId, User currentUser) {
        Project project = projectService.findAndVerifyMembership(projectId, currentUser);
        return sprintRepository.findByProjectOrderByStartDateAsc(project)
                .stream().map(SprintDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public SprintDTO getById(Long id, User currentUser) {
        Sprint sprint = findSprint(id);
        projectService.findAndVerifyMembership(sprint.getProject().getId(), currentUser);
        return SprintDTO.from(sprint);
    }

    public SprintDTO updateStatus(Long id, String newStatus, User currentUser) {
        Sprint sprint = findSprint(id);
        projectService.findAndVerifyMembership(sprint.getProject().getId(), currentUser);

        SprintStatus status;
        try { status = SprintStatus.valueOf(newStatus.toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BadRequestException("Invalid sprint status: " + newStatus); }

        if (status == SprintStatus.ACTIVE) {
            boolean activeExists = sprintRepository.existsByProjectAndStatus(sprint.getProject(), SprintStatus.ACTIVE);
            if (activeExists && sprint.getStatus() != SprintStatus.ACTIVE) {
                throw new BadRequestException("A sprint is already active for this project");
            }
        }

        sprint.setStatus(status);
        return SprintDTO.from(sprintRepository.save(sprint));
    }

    public SprintDTO update(Long id, User currentUser, CreateSprintRequest request) {
        Sprint sprint = findSprint(id);
        projectService.findAndVerifyMembership(sprint.getProject().getId(), currentUser);

        if (request.getName() != null) sprint.setName(request.getName());
        if (request.getGoal() != null) sprint.setGoal(request.getGoal());
        if (request.getStartDate() != null) sprint.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) sprint.setEndDate(request.getEndDate());

        if (sprint.getStartDate() != null && sprint.getEndDate() != null
                && sprint.getEndDate().isBefore(sprint.getStartDate())) {
            throw new BadRequestException("Sprint end date must be after start date");
        }
        return SprintDTO.from(sprintRepository.save(sprint));
    }

    public void delete(Long id, User currentUser) {
        Sprint sprint = findSprint(id);
        projectService.findAndVerifyMembership(sprint.getProject().getId(), currentUser);
        sprintRepository.delete(sprint);
    }

    private Sprint findSprint(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
    }
}
