package com.management_system.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.response.ProjectDailyMetricsResponse;
import com.management_system.service.ProjectDailyMetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects/metrics")
@RequiredArgsConstructor
public class ProjectDailyMetricsController {
    private final ProjectDailyMetricsService metricsService;

    @GetMapping("/project/{projectId}/date/{reportDate}")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProjectDailyMetricsResponse> getMetricsForProject(
            @PathVariable UUID projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        ProjectDailyMetricsResponse response = metricsService.getMetricsForProject(projectId, reportDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}/range")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ProjectDailyMetricsResponse>> getMetricsForProjectDateRange(
            @PathVariable UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ProjectDailyMetricsResponse> response = metricsService.getMetricsForProjectDateRange(projectId, startDate,
                endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/project/{projectId}/calculate/{reportDate}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> calculateAndSaveMetrics(
            @PathVariable UUID projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        metricsService.calculateAndSaveMetrics(projectId, reportDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top-projects")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ProjectDailyMetricsResponse>> getTopProjectsByCompletionRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProjectDailyMetricsResponse> response = metricsService.getTopProjectsByCompletionRate(reportDate, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ProjectDailyMetricsResponse>> getMetricsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ProjectDailyMetricsResponse> response = metricsService.getMetricsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
