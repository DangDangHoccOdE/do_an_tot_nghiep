package com.management_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.management_system.dto.response.ProjectDailyMetricsResponse;
import com.management_system.entity.DailyTask;
import com.management_system.entity.Project;
import com.management_system.entity.ProjectDailyMetrics;
import com.management_system.enums.TaskStatus;
import com.management_system.exception.ResourceNotFoundException;
import com.management_system.repository.ProjectDailyMetricsRepository;
import com.management_system.repository.DailyTaskRepository;
import com.management_system.repository.ProjectRepository;
import com.management_system.service.ProjectDailyMetricsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectDailyMetricsServiceImpl implements ProjectDailyMetricsService {
    private final ProjectDailyMetricsRepository metricsRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectDailyMetricsResponse getMetricsForProject(UUID projectId, LocalDate reportDate) {
        ProjectDailyMetrics metrics = metricsRepository.findByProjectIdAndDate(projectId, reportDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Metrics not found for this project and date"));
        return mapToResponse(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDailyMetricsResponse> getMetricsForProjectDateRange(UUID projectId, LocalDate startDate,
            LocalDate endDate) {
        return metricsRepository.findByProjectIdAndDateRange(projectId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void calculateAndSaveMetrics(UUID projectId, LocalDate reportDate) {
        // Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Get all tasks for the project on the given date
        List<DailyTask> tasks = dailyTaskRepository.findByProjectIdAndDate(projectId, reportDate);

        if (tasks.isEmpty()) {
            // Create empty metrics if no tasks
            ProjectDailyMetrics metrics = new ProjectDailyMetrics();
            metrics.setProjectId(projectId);
            metrics.setReportDate(reportDate);
            metrics.setTotalTasks(0);
            metrics.setCompletedTasks(0);
            metrics.setCompletionRate(BigDecimal.ZERO);
            metricsRepository.save(metrics);
            return;
        }

        // Calculate metrics
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        int inProgressTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        int blockedTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();
        int pendingTasks = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();

        BigDecimal completionRate = totalTasks > 0
                ? BigDecimal.valueOf((double) completedTasks / totalTasks * 100).setScale(2,
                        java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalEstimatedHours = tasks.stream()
                .map(t -> BigDecimal.valueOf(t.getEstimatedHours() != null ? t.getEstimatedHours() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<UUID> teamMembers = tasks.stream()
                .map(DailyTask::getAssignedTo)
                .collect(Collectors.toSet());

        // Calculate productivity index
        BigDecimal teamProductivityIndex = BigDecimal.ZERO;
        if (totalEstimatedHours.compareTo(BigDecimal.ZERO) > 0) {
            teamProductivityIndex = completionRate.divide(new BigDecimal(100), 2,
                    java.math.RoundingMode.HALF_UP);
        }

        // Check if metrics exist for this date
        ProjectDailyMetrics metrics = metricsRepository.findByProjectIdAndDate(projectId, reportDate)
                .orElse(new ProjectDailyMetrics());

        metrics.setProjectId(projectId);
        metrics.setReportDate(reportDate);
        metrics.setTotalTasks(totalTasks);
        metrics.setCompletedTasks(completedTasks);
        metrics.setInProgressTasks(inProgressTasks);
        metrics.setBlockedTasks(blockedTasks);
        metrics.setPendingTasks(pendingTasks);
        metrics.setCompletionRate(completionRate);
        metrics.setTotalEstimatedHours(totalEstimatedHours);
        metrics.setTeamProductivityIndex(teamProductivityIndex);
        metrics.setTeamMembersAssigned(teamMembers.size());

        metricsRepository.save(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDailyMetricsResponse> getTopProjectsByCompletionRate(LocalDate reportDate, int limit) {
        return metricsRepository.findTopProjectsByCompletionRate(reportDate, limit).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDailyMetricsResponse> getMetricsByDateRange(LocalDate startDate, LocalDate endDate) {
        return metricsRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProjectDailyMetricsResponse mapToResponse(ProjectDailyMetrics metrics) {
        Project project = projectRepository.findById(metrics.getProjectId()).orElse(null);
        return ProjectDailyMetricsResponse.builder()
                .id(metrics.getId())
                .projectId(metrics.getProjectId())
                .projectName(project != null ? project.getProjectName() : "Unknown")
                .reportDate(metrics.getReportDate())
                .totalTasks(metrics.getTotalTasks())
                .completedTasks(metrics.getCompletedTasks())
                .inProgressTasks(metrics.getInProgressTasks())
                .blockedTasks(metrics.getBlockedTasks())
                .pendingTasks(metrics.getPendingTasks())
                .completionRate(metrics.getCompletionRate())
                .totalEstimatedHours(metrics.getTotalEstimatedHours())
                .totalCompletedHours(metrics.getTotalCompletedHours())
                .teamProductivityIndex(metrics.getTeamProductivityIndex())
                .teamMembersAssigned(metrics.getTeamMembersAssigned())
                .createdAt(metrics.getCreatedAt())
                .updatedAt(metrics.getUpdatedAt())
                .build();
    }
}
