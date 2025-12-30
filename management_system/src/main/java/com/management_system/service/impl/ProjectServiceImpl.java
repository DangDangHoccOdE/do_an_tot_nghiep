package com.management_system.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.management_system.dto.request.ProjectRequest;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.ProjectResponse;
import com.management_system.entity.Project;
import com.management_system.entity.enums.ProjectStatus;
import com.management_system.repository.ProjectRepository;
import com.management_system.service.inter.IProjectService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private final ProjectRepository projectRepository;

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
                            .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS || p.getStatus() == ProjectStatus.APPROVED)
                            .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
                            .collect(Collectors.toList());
                    break;
                case "future":
                    filtered = all.stream()
                            .filter(p -> p.getStatus() == ProjectStatus.PENDING || (p.getStartDate() != null && p.getStartDate().isAfter(today)))
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
    public ProjectResponse get(UUID id) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return toResponse(project);
    }

    @Override
    public ProjectResponse create(ProjectRequest request) {
        Project project = new Project();
        applyRequest(project, request);
        return toResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
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
