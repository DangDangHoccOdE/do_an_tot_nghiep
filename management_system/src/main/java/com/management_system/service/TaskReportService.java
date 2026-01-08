package com.management_system.service;

import com.management_system.dto.request.TaskReportRequest;
import com.management_system.dto.response.TaskReportResponse;
import com.management_system.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskReportService {
    TaskReportResponse createTaskReport(TaskReportRequest request, UUID createdBy);

    TaskReportResponse updateTaskReport(UUID reportId, TaskReportRequest request, UUID updatedBy);

    TaskReportResponse getTaskReport(UUID reportId);

    List<TaskReportResponse> getReportsByDailyTask(UUID dailyTaskId);

    List<TaskReportResponse> getReportsByReporter(UUID reporterId);

    List<TaskReportResponse> getReportsByStatus(ReportStatus status);

    List<TaskReportResponse> getReportsByDateRange(UUID reporterId, LocalDateTime startDate, LocalDateTime endDate);

    void deleteTaskReport(UUID reportId);

    int getReportCountByTask(UUID dailyTaskId);
}
