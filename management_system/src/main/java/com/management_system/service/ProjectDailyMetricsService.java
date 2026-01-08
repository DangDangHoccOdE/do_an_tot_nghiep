package com.management_system.service;

import com.management_system.dto.response.ProjectDailyMetricsResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProjectDailyMetricsService {
    ProjectDailyMetricsResponse getMetricsForProject(UUID projectId, LocalDate reportDate);

    List<ProjectDailyMetricsResponse> getMetricsForProjectDateRange(UUID projectId, LocalDate startDate,
            LocalDate endDate);

    void calculateAndSaveMetrics(UUID projectId, LocalDate reportDate);

    List<ProjectDailyMetricsResponse> getTopProjectsByCompletionRate(LocalDate reportDate, int limit);

    List<ProjectDailyMetricsResponse> getMetricsByDateRange(LocalDate startDate, LocalDate endDate);
}
