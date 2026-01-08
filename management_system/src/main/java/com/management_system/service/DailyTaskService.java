package com.management_system.service;

import com.management_system.dto.request.DailyTaskRequest;
import com.management_system.dto.response.DailyTaskResponse;
import com.management_system.enums.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyTaskService {
    DailyTaskResponse createDailyTask(DailyTaskRequest request, UUID createdBy);

    DailyTaskResponse updateDailyTask(UUID taskId, DailyTaskRequest request, UUID updatedBy);

    DailyTaskResponse getDailyTask(UUID taskId);

    List<DailyTaskResponse> getTasksByProject(UUID projectId);

    List<DailyTaskResponse> getTasksByProjectAndDate(UUID projectId, LocalDate taskDate);

    List<DailyTaskResponse> getTasksByAssignedUser(UUID userId);

    List<DailyTaskResponse> getTasksByAssignedUserAndDate(UUID userId, LocalDate taskDate);

    List<DailyTaskResponse> getTasksByDateRange(UUID projectId, LocalDate startDate, LocalDate endDate);

    List<DailyTaskResponse> getTasksByStatus(UUID projectId, TaskStatus status);

    void deleteDailyTask(UUID taskId);

    int getTaskCountByProjectAndDate(UUID projectId, LocalDate taskDate);

    int getTaskCountByStatusAndDate(UUID projectId, LocalDate taskDate, TaskStatus status);
}
