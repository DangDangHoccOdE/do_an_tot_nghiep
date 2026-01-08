package com.management_system.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.management_system.core.ValidatorWrapper;
import com.management_system.dto.request.ProjectRequest;
import com.management_system.utils.MessageUtil;
import org.springframework.context.i18n.LocaleContextHolder;
import com.management_system.dto.response.ProjectMemberResponse;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.ProjectResponse;
import com.management_system.entity.Project;
import com.management_system.entity.ProjectMember;
import com.management_system.entity.TeamMember;
import com.management_system.entity.User;
import com.management_system.entity.enums.ITRole;
import com.management_system.entity.enums.ProjectStatus;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.ProjectMemberRepository;
import com.management_system.repository.TeamMemberRepository;
import com.management_system.repository.UserRepository;
import com.management_system.service.inter.IProjectService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ValidatorWrapper validator;
    private final MessageUtil messageUtil;

    @Override
    public PageResponse<ProjectResponse> getPage(String status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<Project> all = projectRepository.findAllByDeleteFlagFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
        LocalDate today = LocalDate.now();

        List<Project> filtered = all;
        if (status != null) {
            switch (status.toLowerCase()) {
                case "current":
                    // Hiển thị tất cả dự án trừ trạng thái PENDING (đang duyệt)
                    filtered = all.stream()
                            .filter(p -> p.getStatus() != ProjectStatus.PENDING)
                            .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
                            .collect(Collectors.toList());
                    break;
                case "future":
                    filtered = all.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.PENDING
                                    || (p.getStatus() != ProjectStatus.DONE && p.getStatus() != ProjectStatus.CANCELLED
                                            && p.getStartDate() != null && p.getStartDate().isAfter(today)))
                            .collect(Collectors.toList());
                    break;
                default:
            }
        }

        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        List<ProjectResponse> content = new ArrayList<>();
        if (fromIndex < filtered.size()) {
            content = filtered.subList(fromIndex, toIndex).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        int totalPages = (int) Math.ceil((double) filtered.size() / safeSize);

        return new PageResponse<>(content, filtered.size(), totalPages, safePage, safeSize);
    }

    @Override
    public PageResponse<ProjectResponse> getMyProjects(UUID userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        // Tìm tất cả team mà user tham gia
        List<TeamMember> teamMembers = teamMemberRepository.findAllByUserIdAndDeleteFlagFalse(userId);
        List<UUID> teamIds = teamMembers.stream()
                .map(TeamMember::getTeamId)
                .collect(Collectors.toList());

        // Nếu user không thuộc team nào, trả về danh sách rỗng
        if (teamIds.isEmpty()) {
            return new PageResponse<>(new ArrayList<>(), 0, 0, safePage, safeSize);
        }

        // Tìm tất cả project mà user tham gia thông qua team
        List<Project> projects = projectRepository.findAllByTeamIdInAndDeleteFlagFalse(teamIds);

        // Sắp xếp theo ngày tạo mới nhất
        projects.sort((p1, p2) -> {
            if (p1.getCreatedAt() == null && p2.getCreatedAt() == null)
                return 0;
            if (p1.getCreatedAt() == null)
                return 1;
            if (p2.getCreatedAt() == null)
                return -1;
            return p2.getCreatedAt().compareTo(p1.getCreatedAt());
        });

        // Phân trang
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, projects.size());
        List<ProjectResponse> content = new ArrayList<>();
        if (fromIndex < projects.size()) {
            content = projects.subList(fromIndex, toIndex).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        int totalPages = (int) Math.ceil((double) projects.size() / safeSize);

        return new PageResponse<>(content, projects.size(), totalPages, safePage, safeSize);
    }

    @Override
    public ProjectResponse get(UUID id) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return toResponse(project);
    }

    @Override
    public ProjectResponse create(ProjectRequest request) {
        validateRequest(request, false);
        Project project = new Project();
        applyRequest(project, request);
        Project saved = projectRepository.save(project);
        syncMembers(saved, request.getMemberIds());
        return toResponse(saved);
    }

    @Override
    public ProjectResponse createFuture(ProjectRequest request) {
        validateRequest(request, true);
        Project project = new Project();
        applyRequest(project, request);
        Project saved = projectRepository.save(project);
        syncMembers(saved, request.getMemberIds());
        return toResponse(saved);
    }

    @Override
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        validateRequest(request, false);
        applyRequest(project, request);
        Project saved = projectRepository.save(project);
        syncMembers(saved, request.getMemberIds());
        return toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        project.setDeleteFlag(true);
        projectRepository.save(project);
    }

    @Override
    public boolean existsByNameExcludingId(String projectName, UUID excludeId) {
        if (projectName == null || projectName.trim().isEmpty()) {
            return false;
        }

        String trimmedName = projectName.trim();

        if (excludeId == null) {
            // Tạo mới: check toàn bộ
            return projectRepository.existsByProjectNameAndDeleteFlagFalse(trimmedName);
        } else {
            // Edit: bỏ qua chính nó
            return projectRepository.existsByProjectNameAndIdNotAndDeleteFlagFalse(trimmedName, excludeId);
        }
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setClientId(request.getClientId());
        project.setTeamId(request.getTeamId());
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setBudgetEstimated(request.getBudgetEstimated());
        project.setTimelineEstimated(request.getTimelineEstimated());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
    }

    private void validateRequest(ProjectRequest request, boolean enforceFutureDate) {
        validator.validate(request);

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        LocalDate today = LocalDate.now();

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    messageUtil.getMessage("project.error.endDateBeforeStart", null, LocaleContextHolder.getLocale()));
        }

        if (enforceFutureDate) {
            if (startDate == null) {
                throw new IllegalArgumentException(
                        messageUtil.getMessage("project.error.startDateRequired", null,
                                LocaleContextHolder.getLocale()));
            }
            if (!startDate.isAfter(today)) {
                throw new IllegalArgumentException(
                        messageUtil.getMessage("project.error.futureProjectMustStartAfterToday", null,
                                LocaleContextHolder.getLocale()));
            }
            if (request.getStatus() == null) {
                request.setStatus(ProjectStatus.PENDING);
            }
        }

        if (request.getStatus() == ProjectStatus.IN_PROGRESS || request.getStatus() == ProjectStatus.APPROVED) {
            if (startDate != null && startDate.isAfter(today)) {
                throw new IllegalArgumentException(
                        messageUtil.getMessage("project.error.cannotMarkInProgressBeforeStartDate", null,
                                LocaleContextHolder.getLocale()));
            }
        }
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .clientId(project.getClientId())
                .teamId(project.getTeamId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .budgetEstimated(project.getBudgetEstimated())
                .timelineEstimated(project.getTimelineEstimated())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .members(buildMemberResponses(project.getId()))
                .build();
    }

    private void syncMembers(Project project, List<UUID> memberIds) {
        if (memberIds == null) {
            return;
        }

        Set<UUID> incoming = new HashSet<>(memberIds);

        List<ProjectMember> activeMembers = projectMemberRepository
                .findAllByProjectIdAndDeleteFlagFalse(project.getId());

        // Soft-delete removed members
        activeMembers.stream()
                .filter(member -> !incoming.contains(member.getUserId()))
                .forEach(member -> {
                    member.setDeleteFlag(true);
                    projectMemberRepository.save(member);
                });

        Map<UUID, ITRole> userRoles = userRepository.findAllById(incoming).stream()
                .collect(Collectors.toMap(User::getId, User::getItRole, (a, b) -> a, HashMap::new));

        for (UUID userId : incoming) {
            ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), userId)
                    .orElseGet(() -> {
                        ProjectMember newMember = new ProjectMember();
                        newMember.setProjectId(project.getId());
                        newMember.setUserId(userId);
                        return newMember;
                    });

            member.setDeleteFlag(false);
            member.setItRole(userRoles.get(userId));
            projectMemberRepository.save(member);
        }
    }

    private List<ProjectMemberResponse> buildMemberResponses(UUID projectId) {
        List<ProjectMember> members = projectMemberRepository.findAllByProjectIdAndDeleteFlagFalse(projectId);
        if (members.isEmpty()) {
            return List.of();
        }

        Map<UUID, User> userMap = userRepository
                .findAllById(members.stream().map(ProjectMember::getUserId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a, HashMap::new));

        return members.stream().map(member -> {
            User user = userMap.get(member.getUserId());
            String fullName = null;
            if (user != null) {
                String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                String lastName = user.getLastName() != null ? user.getLastName() : "";
                fullName = (firstName + " " + lastName).trim();
            }

            return ProjectMemberResponse.builder()
                    .userId(member.getUserId())
                    .fullName(fullName == null || fullName.isBlank() ? null : fullName)
                    .email(user != null ? user.getEmail() : null)
                    .phone(user != null ? user.getPhone() : null)
                    .avatar(user != null ? user.getAvatar() : null)
                    .itRole(member.getItRole())
                    .build();
        }).collect(Collectors.toList());
    }
}
