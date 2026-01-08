package com.management_system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.management_system.dto.request.DailyTaskRequest;
import com.management_system.dto.response.DailyTaskResponse;
import com.management_system.entity.DailyTask;
import com.management_system.entity.User;
import com.management_system.enums.TaskStatus;
import com.management_system.exception.ResourceNotFoundException;
import com.management_system.repository.DailyTaskRepository;
import com.management_system.repository.UserRepository;
import com.management_system.repository.ProjectRepository;
import com.management_system.service.DailyTaskService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyTaskServiceImpl implements DailyTaskService {
    private final DailyTaskRepository dailyTaskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public DailyTaskResponse createDailyTask(DailyTaskRequest request, UUID createdBy) {
        // Validate project exists
        projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate user exists
        User assignedUser = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DailyTask dailyTask = new DailyTask();
        dailyTask.setProjectId(request.getProjectId());
        dailyTask.setAssignedTo(request.getAssignedTo());
        dailyTask.setTitle(request.getTitle());
        dailyTask.setDescription(request.getDescription());
        dailyTask.setTaskDate(request.getTaskDate());
        dailyTask.setPriority(request.getPriority());
        dailyTask.setEstimatedHours(
                request.getEstimatedHours() != null
                        ? request.getEstimatedHours().intValue()
                        : null);
        dailyTask.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        dailyTask.setCreatedBy(createdBy);
        dailyTask.setUpdatedBy(createdBy);

        DailyTask saved = dailyTaskRepository.save(dailyTask);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DailyTaskResponse updateDailyTask(UUID taskId, DailyTaskRequest request, UUID updatedBy) {
        DailyTask dailyTask = dailyTaskRepository.findByIdNotDeleted(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily task not found"));

        // Validate user exists if changed
        if (!dailyTask.getAssignedTo().equals(request.getAssignedTo())) {
            userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        dailyTask.setTitle(request.getTitle());
        dailyTask.setDescription(request.getDescription());
        dailyTask.setTaskDate(request.getTaskDate());
        dailyTask.setPriority(request.getPriority());
        dailyTask.setEstimatedHours(
                request.getEstimatedHours() != null
                        ? request.getEstimatedHours().intValue()
                        : null);
        dailyTask.setStatus(request.getStatus());
        dailyTask.setAssignedTo(request.getAssignedTo());
        dailyTask.setUpdatedBy(updatedBy);

        DailyTask updated = dailyTaskRepository.save(dailyTask);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyTaskResponse getDailyTask(UUID taskId) {
        DailyTask dailyTask = dailyTaskRepository.findByIdNotDeleted(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily task not found"));
        return mapToResponse(dailyTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByProject(UUID projectId) {
        return dailyTaskRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByProjectAndDate(UUID projectId, LocalDate taskDate) {
        return dailyTaskRepository.findByProjectIdAndDate(projectId, taskDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByAssignedUser(UUID userId) {
        return dailyTaskRepository.findByAssignedTo(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByAssignedUserAndDate(UUID userId, LocalDate taskDate) {
        return dailyTaskRepository.findByAssignedToAndDate(userId, taskDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByDateRange(UUID projectId, LocalDate startDate, LocalDate endDate) {
        return dailyTaskRepository.findByProjectIdAndDateRange(projectId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTaskResponse> getTasksByStatus(UUID projectId, TaskStatus status) {
        return dailyTaskRepository.findByStatusAndProject(status, projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDailyTask(UUID taskId) {
        DailyTask dailyTask = dailyTaskRepository.findByIdNotDeleted(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily task not found"));
        dailyTask.setDeleteFlag(true);
        dailyTaskRepository.save(dailyTask);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTaskCountByProjectAndDate(UUID projectId, LocalDate taskDate) {
        return dailyTaskRepository.countTasksByProjectAndDate(projectId, taskDate);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTaskCountByStatusAndDate(UUID projectId, LocalDate taskDate, TaskStatus status) {
        return dailyTaskRepository.countTasksByStatusAndDate(projectId, taskDate, status);
    }

    private DailyTaskResponse mapToResponse(DailyTask dailyTask) {
        User user = userRepository.findById(dailyTask.getAssignedTo()).orElse(null);
        return DailyTaskResponse.builder()
                .id(dailyTask.getId())
                .projectId(dailyTask.getProjectId())
                .assignedTo(dailyTask.getAssignedTo())
                .assignedToUserName(user != null ? (user.getLastName() + " " + user.getFirstName()) : "Unknown")
                .title(dailyTask.getTitle())
                .description(dailyTask.getDescription())
                .taskDate(dailyTask.getTaskDate())
                .priority(dailyTask.getPriority())
                .estimatedHours(
                        dailyTask.getEstimatedHours() != null
                                ? dailyTask.getEstimatedHours().doubleValue()
                                : null)
                .status(dailyTask.getStatus())
                .createdAt(dailyTask.getCreatedAt())
                .updatedAt(dailyTask.getUpdatedAt())
                .build();
    }
}
