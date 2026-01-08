package com.management_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.management_system.dto.request.TaskReportRequest;
import com.management_system.dto.response.TaskReportResponse;
import com.management_system.enums.ReportStatus;
import com.management_system.security.Endpoints;
import com.management_system.service.TaskReportService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoints.TASK_REPORTS)
@RequiredArgsConstructor
public class TaskReportController {
    private final TaskReportService taskReportService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TaskReportResponse> createTaskReport(@Valid @RequestBody TaskReportRequest request) {
        String userId = getCurrentUserId();
        TaskReportResponse response = taskReportService.createTaskReport(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reportId}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TaskReportResponse> updateTaskReport(
            @PathVariable UUID reportId,
            @Valid @RequestBody TaskReportRequest request) {
        String userId = getCurrentUserId();
        TaskReportResponse response = taskReportService.updateTaskReport(reportId, request, UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TaskReportResponse> getTaskReport(@PathVariable UUID reportId) {
        TaskReportResponse response = taskReportService.getTaskReport(reportId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/{dailyTaskId}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TaskReportResponse>> getReportsByDailyTask(@PathVariable UUID dailyTaskId) {
        List<TaskReportResponse> reports = taskReportService.getReportsByDailyTask(dailyTaskId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reporter/{reporterId}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TaskReportResponse>> getReportsByReporter(@PathVariable UUID reporterId) {
        List<TaskReportResponse> reports = taskReportService.getReportsByReporter(reporterId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TaskReportResponse>> getReportsByStatus(@PathVariable ReportStatus status) {
        List<TaskReportResponse> reports = taskReportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reporter/{reporterId}/range")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TaskReportResponse>> getReportsByDateRange(
            @PathVariable UUID reporterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TaskReportResponse> reports = taskReportService.getReportsByDateRange(reporterId, startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTaskReport(@PathVariable UUID reportId) {
        taskReportService.deleteTaskReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/task/{dailyTaskId}/count")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PROJECT_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Integer> getReportCount(@PathVariable UUID dailyTaskId) {
        int count = taskReportService.getReportCountByTask(dailyTaskId);
        return ResponseEntity.ok(count);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
