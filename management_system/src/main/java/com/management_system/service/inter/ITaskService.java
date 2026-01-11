package com.management_system.service.inter;

import java.util.List;
import java.util.UUID;

import com.management_system.dto.request.TaskRequest;
import com.management_system.dto.response.TaskResponse;

public interface ITaskService {
    List<TaskResponse> getByProject(UUID projectId);

    List<TaskResponse> getByAssignee(UUID userId);

    List<TaskResponse> getByProjectsOfUser(UUID userId);

    TaskResponse get(UUID id);

    TaskResponse create(TaskRequest request);

    TaskResponse update(UUID id, TaskRequest request);

    void delete(UUID id);
}
