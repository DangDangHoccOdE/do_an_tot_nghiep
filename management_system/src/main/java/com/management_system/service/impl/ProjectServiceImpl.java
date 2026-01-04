package com.management_system.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.management_system.core.ValidatorWrapper;
import com.management_system.dto.request.ProjectRequest;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.ProjectResponse;
import com.management_system.entity.Project;
import com.management_system.entity.TeamMember;
import com.management_system.entity.enums.ProjectStatus;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.TeamMemberRepository;
import com.management_system.service.inter.IProjectService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ValidatorWrapper validator;

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
                    filtered = all.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS
                                    || p.getStatus() == ProjectStatus.APPROVED)
                            .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
                            .collect(Collectors.toList());
                    break;
                case "future":
                    filtered = all.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.PENDING
                                    || (p.getStartDate() != null && p.getStartDate().isAfter(today)))
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
        return toResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponse createFuture(ProjectRequest request) {
        validateRequest(request, true);
        Project project = new Project();
        applyRequest(project, request);
        return toResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        validateRequest(request, false);
        applyRequest(project, request);
        return toResponse(projectRepository.save(project));
    }

    @Override
    public void delete(UUID id) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        project.setDeleteFlag(true);
        projectRepository.save(project);
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
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (enforceFutureDate) {
            if (startDate == null) {
                throw new IllegalArgumentException("Start date is required for future projects");
            }
            if (!startDate.isAfter(today)) {
                throw new IllegalArgumentException("Future projects must start after today");
            }
            if (request.getStatus() == null) {
                request.setStatus(ProjectStatus.PENDING);
            }
        }

        if (request.getStatus() == ProjectStatus.IN_PROGRESS || request.getStatus() == ProjectStatus.APPROVED) {
            if (startDate != null && startDate.isAfter(today)) {
                throw new IllegalArgumentException("Cannot mark project in progress before its start date");
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
                .build();
    }
}
