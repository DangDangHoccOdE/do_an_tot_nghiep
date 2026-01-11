package com.management_system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.management_system.dto.request.TeamMemberRequest;
import com.management_system.dto.request.TeamRequest;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.TeamMemberResponse;
import com.management_system.dto.response.TeamResponse;
import com.management_system.entity.Project;
import com.management_system.entity.Team;
import com.management_system.entity.TeamMember;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.TeamMemberRepository;
import com.management_system.repository.TeamRepository;
import com.management_system.service.inter.ITeamService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;

    @Override
    public PageResponse<TeamResponse> getPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<Team> all = teamRepository.findAllByDeleteFlagFalse(Sort.by(Sort.Direction.ASC, "name"));

        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, all.size());
        List<TeamResponse> content = new ArrayList<>();
        if (fromIndex < all.size()) {
            content = all.subList(fromIndex, toIndex)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        int totalPages = (int) Math.ceil((double) all.size() / safeSize);

        return new PageResponse<>(content, all.size(), totalPages, safePage, safeSize);
    }

    @Override
    public TeamResponse get(UUID id) {
        Team team = teamRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        return toResponse(team);
    }

    @Override
    public TeamResponse create(TeamRequest request) {
        // Validate project exists
        Project project = projectRepository.findByIdAndDeleteFlagFalse(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + request.getProjectId()));

        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setProjectId(project.getId());
        return toResponse(teamRepository.save(team));
    }

    @Override
    public TeamResponse update(UUID id, TeamRequest request) {
        Team team = teamRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // Validate project exists if projectId is changed
        if (!team.getProjectId().equals(request.getProjectId())) {
            Project project = projectRepository.findByIdAndDeleteFlagFalse(request.getProjectId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Project not found with ID: " + request.getProjectId()));
            team.setProjectId(project.getId());
        }

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        return toResponse(teamRepository.save(team));
    }

    @Override
    public TeamResponse addMember(UUID teamId, TeamMemberRequest request) {
        Team team = teamRepository.findByIdAndDeleteFlagFalse(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        teamMemberRepository.findByTeamIdAndUserIdAndDeleteFlagFalse(teamId, request.getUserId())
                .ifPresent(tm -> {
                    throw new IllegalArgumentException("Member already exists in team");
                });

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(request.getUserId());
        member.setRoleInTeam(request.getRoleInTeam());
        teamMemberRepository.save(member);

        return toResponse(team);
    }

    @Override
    public void removeMember(UUID teamId, UUID userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserIdAndDeleteFlagFalse(teamId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Team member not found"));
        member.setDeleteFlag(true);
        teamMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Team team = teamRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        softDeleteTeam(team);
    }

    @Override
    @Transactional
    public void deleteBulk(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Team> teams = teamRepository.findAllById(ids).stream()
                .filter(team -> !Boolean.TRUE.equals(team.getDeleteFlag()))
                .collect(Collectors.toList());

        long distinctRequested = ids.stream().distinct().count();
        if (teams.size() != distinctRequested) {
            throw new EntityNotFoundException("Team not found");
        }

        teams.forEach(this::softDeleteTeam);
    }

    private void softDeleteTeam(Team team) {
        team.setDeleteFlag(true);
        teamRepository.save(team);

        List<TeamMember> members = teamMemberRepository.findAllByTeamIdAndDeleteFlagFalse(team.getId());
        if (!members.isEmpty()) {
            members.forEach(member -> member.setDeleteFlag(true));
            teamMemberRepository.saveAll(members);
        }
    }

    private TeamResponse toResponse(Team team) {
        List<TeamMemberResponse> members = teamMemberRepository.findAllByTeamIdAndDeleteFlagFalse(team.getId())
                .stream()
                .map(tm -> TeamMemberResponse.builder()
                        .userId(tm.getUserId())
                        .roleInTeam(tm.getRoleInTeam())
                        .build())
                .collect(Collectors.toList());

        // Get project info
        String projectName = null;
        if (team.getProjectId() != null) {
            projectName = projectRepository.findByIdAndDeleteFlagFalse(team.getProjectId())
                    .map(Project::getProjectName)
                    .orElse(null);
        }

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .projectId(team.getProjectId())
                .projectName(projectName)
                .members(members)
                .build();
    }
}
