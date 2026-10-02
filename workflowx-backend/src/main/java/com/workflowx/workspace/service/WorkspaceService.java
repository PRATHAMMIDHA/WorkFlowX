package com.workflowx.workspace.service;

import com.workflowx.common.enums.WorkspaceRole;
import com.workflowx.exception.BadRequestException;
import com.workflowx.exception.ConflictException;
import com.workflowx.exception.ResourceNotFoundException;
import com.workflowx.exception.UnauthorizedException;
import com.workflowx.user.entity.User;
import com.workflowx.user.repository.UserRepository;
import com.workflowx.workspace.dto.*;
import com.workflowx.workspace.entity.Workspace;
import com.workflowx.workspace.entity.WorkspaceMember;
import com.workflowx.workspace.repository.WorkspaceMemberRepository;
import com.workflowx.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    public WorkspaceDTO create(User currentUser, CreateWorkspaceRequest request) {
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();
        workspaceRepository.save(workspace);

        // Add owner as first member
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(currentUser)
                .role(WorkspaceRole.OWNER)
                .build();
        memberRepository.save(ownerMember);
        workspace.getMembers().add(ownerMember);

        return WorkspaceDTO.from(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceDTO> getMyWorkspaces(User currentUser) {
        return workspaceRepository.findAllByMember(currentUser)
                .stream().map(WorkspaceDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceDTO getById(Long id, User currentUser) {
        Workspace workspace = findAndVerifyMembership(id, currentUser);
        return WorkspaceDTO.from(workspace);
    }

    public WorkspaceDTO update(Long id, User currentUser, CreateWorkspaceRequest request) {
        Workspace workspace = findAndVerifyOwnership(id, currentUser);
        if (request.getName() != null) workspace.setName(request.getName());
        if (request.getDescription() != null) workspace.setDescription(request.getDescription());
        return WorkspaceDTO.from(workspaceRepository.save(workspace));
    }

    public void delete(Long id, User currentUser) {
        Workspace workspace = findAndVerifyOwnership(id, currentUser);
        workspaceRepository.delete(workspace);
    }

    public WorkspaceMemberDTO addMember(Long workspaceId, User currentUser, AddMemberRequest request) {
        Workspace workspace = findAndVerifyAdminOrOwner(workspaceId, currentUser);
        User newUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (memberRepository.existsByWorkspaceAndUser(workspace, newUser)) {
            throw new ConflictException("User is already a member of this workspace");
        }

        WorkspaceRole role;
        try {
            role = WorkspaceRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = WorkspaceRole.MEMBER;
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(newUser)
                .role(role)
                .build();
        return WorkspaceMemberDTO.from(memberRepository.save(member));
    }

    public void removeMember(Long workspaceId, Long userId, User currentUser) {
        Workspace workspace = findAndVerifyAdminOrOwner(workspaceId, currentUser);
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (workspace.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Cannot remove the workspace owner");
        }

        WorkspaceMember member = memberRepository.findByWorkspaceAndUser(workspace, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in workspace"));
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberDTO> getMembers(Long workspaceId, User currentUser) {
        Workspace workspace = findAndVerifyMembership(workspaceId, currentUser);
        return memberRepository.findByWorkspace(workspace)
                .stream().map(WorkspaceMemberDTO::from).toList();
    }

    // ── Private helpers ──────────────────────────────────────────────

    private Workspace findAndVerifyMembership(Long id, User user) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
        if (!memberRepository.existsByWorkspaceAndUser(workspace, user)) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
        return workspace;
    }

    private Workspace findAndVerifyOwnership(Long id, User user) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
        if (!workspace.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the workspace owner can perform this action");
        }
        return workspace;
    }

    private Workspace findAndVerifyAdminOrOwner(Long id, User user) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
        memberRepository.findByWorkspaceAndUser(workspace, user)
                .filter(m -> m.getRole() == WorkspaceRole.OWNER || m.getRole() == WorkspaceRole.ADMIN)
                .orElseThrow(() -> new UnauthorizedException("Only workspace admins or owners can perform this action"));
        return workspace;
    }
}
