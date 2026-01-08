package com.management_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.management_system.dto.request.TaskReportRequest;
import com.management_system.dto.response.TaskReportResponse;
import com.management_system.entity.TaskReport;
import com.management_system.entity.User;
import com.management_system.enums.ReportStatus;
import com.management_system.exception.ResourceNotFoundException;
import com.management_system.repository.TaskReportRepository;
import com.management_system.repository.UserRepository;
import com.management_system.repository.DailyTaskRepository;
import com.management_system.service.TaskReportService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskReportServiceImpl implements TaskReportService {
    private final TaskReportRepository taskReportRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskReportResponse createTaskReport(TaskReportRequest request, UUID createdBy) {
        // Validate daily task exists
        dailyTaskRepository.findByIdNotDeleted(request.getDailyTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Daily task not found"));

        // Validate reporter exists
        User reporter = userRepository.findById(createdBy)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter user not found"));

        TaskReport taskReport = new TaskReport();
        taskReport.setDailyTaskId(request.getDailyTaskId());
        taskReport.setReportedBy(createdBy);
        taskReport.setStatus(request.getStatus());
        taskReport.setCompletedHours(request.getCompletedHours());
        taskReport.setCompletionPercentage(request.getCompletionPercentage());
        taskReport.setNotes(request.getNotes());
        taskReport.setEvidenceLink(request.getEvidenceLink());

        TaskReport saved = taskReportRepository.save(taskReport);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TaskReportResponse updateTaskReport(UUID reportId, TaskReportRequest request, UUID updatedBy) {
        TaskReport taskReport = taskReportRepository.findByIdNotDeleted(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Task report not found"));

        taskReport.setStatus(request.getStatus());
        taskReport.setCompletedHours(request.getCompletedHours());
        taskReport.setCompletionPercentage(request.getCompletionPercentage());
        taskReport.setNotes(request.getNotes());
        taskReport.setEvidenceLink(request.getEvidenceLink());

        TaskReport updated = taskReportRepository.save(taskReport);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskReportResponse getTaskReport(UUID reportId) {
        TaskReport taskReport = taskReportRepository.findByIdNotDeleted(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Task report not found"));
        return mapToResponse(taskReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskReportResponse> getReportsByDailyTask(UUID dailyTaskId) {
        return taskReportRepository.findByDailyTaskId(dailyTaskId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskReportResponse> getReportsByReporter(UUID reporterId) {
        return taskReportRepository.findByReportedBy(reporterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskReportResponse> getReportsByStatus(ReportStatus status) {
        return taskReportRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskReportResponse> getReportsByDateRange(UUID reporterId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return taskReportRepository.findByReportedByAndDateRange(reporterId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTaskReport(UUID reportId) {
        TaskReport taskReport = taskReportRepository.findByIdNotDeleted(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Task report not found"));
        taskReport.setDeleteFlag(true);
        taskReportRepository.save(taskReport);
    }

    @Override
    @Transactional(readOnly = true)
    public int getReportCountByTask(UUID dailyTaskId) {
        return taskReportRepository.countReportsByTask(dailyTaskId);
    }

    private TaskReportResponse mapToResponse(TaskReport taskReport) {
        User reporter = userRepository.findById(taskReport.getReportedBy()).orElse(null);
        return TaskReportResponse.builder()
                .id(taskReport.getId())
                .dailyTaskId(taskReport.getDailyTaskId())
                .reportedBy(taskReport.getReportedBy())
                .reportedByUserName(
                        reporter != null ? (reporter.getLastName() + " " + reporter.getFirstName()) : "Unknown")
                .reportedAt(taskReport.getReportedAt())
                .status(taskReport.getStatus())
                .completedHours(taskReport.getCompletedHours())
                .completionPercentage(taskReport.getCompletionPercentage())
                .notes(taskReport.getNotes())
                .evidenceLink(taskReport.getEvidenceLink())
                .createdAt(taskReport.getCreatedAt())
                .updatedAt(taskReport.getUpdatedAt())
                .build();
    }
}
