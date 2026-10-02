package com.workflowx.team.repository;

import com.workflowx.team.entity.Team;
import com.workflowx.team.entity.TeamMember;
import com.workflowx.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByTeamAndUser(Team team, User user);
    boolean existsByTeamAndUser(Team team, User user);
    List<TeamMember> findByTeam(Team team);
    void deleteByTeamAndUser(Team team, User user);
}
