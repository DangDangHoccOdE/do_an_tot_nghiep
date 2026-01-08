package com.management_system.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.management_system.dto.request.TaskRequest;
import com.management_system.dto.response.TaskResponse;
import com.management_system.entity.enums.TaskStatus;
import com.management_system.entity.Project;
import com.management_system.entity.Task;
import com.management_system.core.ValidatorWrapper;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.TaskRepository;
import com.management_system.service.inter.ITaskService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ValidatorWrapper validator;

    @Override
    public List<TaskResponse> getByProject(UUID projectId) {
        ensureProjectExists(projectId);
        return taskRepository.findAllByProjectIdAndDeleteFlagFalse(projectId, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse get(UUID id) {
        Task task = taskRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        return toResponse(task);
    }

    @Override
    public TaskResponse create(TaskRequest request) {
        validateRequest(request);
        ensureProjectExists(request.getProjectId());
        Task task = new Task();
        applyRequest(task, request);
        return toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = taskRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        validateRequest(request);
        applyRequest(task, request);
        return toResponse(taskRepository.save(task));
    }

    @Override
    public void delete(UUID id) {
        Task task = taskRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setDeleteFlag(true);
        taskRepository.save(task);
    }

    private void applyRequest(Task task, TaskRequest request) {
        if (request.getProjectId() != null) {
            task.setProjectId(request.getProjectId());
        }
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        task.setAssignedToUserId(request.getAssignedToUserId());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
    }

    private void validateRequest(TaskRequest request) {
        validator.validate(request);

        if (request.getStatus() == null) {
            request.setStatus(TaskStatus.TODO);
        }

        if (request.getStartDate() != null && request.getDueDate() != null
                && request.getDueDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Due date must be after start date");
        }
    }

    private void ensureProjectExists(UUID projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId is required");
        }
        projectRepository.findByIdAndDeleteFlagFalse(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assignedToUserId(task.getAssignedToUserId())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .build();
    }
}
