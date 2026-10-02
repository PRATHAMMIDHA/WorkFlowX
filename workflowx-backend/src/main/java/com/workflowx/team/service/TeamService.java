package com.workflowx.team.service;

import com.workflowx.exception.ConflictException;
import com.workflowx.exception.ResourceNotFoundException;
import com.workflowx.exception.UnauthorizedException;
import com.workflowx.team.dto.*;
import com.workflowx.team.entity.Team;
import com.workflowx.team.entity.TeamMember;
import com.workflowx.team.repository.TeamMemberRepository;
import com.workflowx.team.repository.TeamRepository;
import com.workflowx.user.entity.User;
import com.workflowx.user.repository.UserRepository;
import com.workflowx.workspace.entity.Workspace;
import com.workflowx.workspace.repository.WorkspaceMemberRepository;
import com.workflowx.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    public TeamDTO create(User currentUser, CreateTeamRequest request) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", request.getWorkspaceId()));
        if (!workspaceMemberRepository.existsByWorkspaceAndUser(workspace, currentUser)) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .workspace(workspace)
                .build();
        teamRepository.save(team);
        // Creator automatically becomes a member
        TeamMember creator = TeamMember.builder().team(team).user(currentUser).build();
        teamMemberRepository.save(creator);
        team.getMembers().add(creator);
        return TeamDTO.from(team);
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> getByWorkspace(Long workspaceId, User currentUser) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
        if (!workspaceMemberRepository.existsByWorkspaceAndUser(workspace, currentUser)) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
        return teamRepository.findByWorkspace(workspace).stream().map(TeamDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public TeamDTO getById(Long id, User currentUser) {
        Team team = findTeam(id);
        verifyWorkspaceMembership(team, currentUser);
        return TeamDTO.from(team);
    }

    public TeamDTO update(Long id, User currentUser, CreateTeamRequest request) {
        Team team = findTeam(id);
        verifyWorkspaceMembership(team, currentUser);
        if (request.getName() != null) team.setName(request.getName());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
        return TeamDTO.from(teamRepository.save(team));
    }

    public void delete(Long id, User currentUser) {
        Team team = findTeam(id);
        verifyWorkspaceMembership(team, currentUser);
        teamRepository.delete(team);
    }

    public TeamMemberDTO addMember(Long teamId, Long userId, User currentUser) {
        Team team = findTeam(teamId);
        verifyWorkspaceMembership(team, currentUser);
        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (teamMemberRepository.existsByTeamAndUser(team, newUser)) {
            throw new ConflictException("User is already a member of this team");
        }
        TeamMember member = TeamMember.builder().team(team).user(newUser).build();
        return TeamMemberDTO.from(teamMemberRepository.save(member));
    }

    public void removeMember(Long teamId, Long userId, User currentUser) {
        Team team = findTeam(teamId);
        verifyWorkspaceMembership(team, currentUser);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        teamMemberRepository.deleteByTeamAndUser(team, target);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getMembers(Long teamId, User currentUser) {
        Team team = findTeam(teamId);
        verifyWorkspaceMembership(team, currentUser);
        return teamMemberRepository.findByTeam(team).stream().map(TeamMemberDTO::from).toList();
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
    }

    private void verifyWorkspaceMembership(Team team, User user) {
        if (!workspaceMemberRepository.existsByWorkspaceAndUser(team.getWorkspace(), user)) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
    }
}
