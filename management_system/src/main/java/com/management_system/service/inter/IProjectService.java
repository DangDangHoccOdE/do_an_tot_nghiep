package com.management_system.service.inter;

import java.util.UUID;

import com.management_system.dto.request.ProjectRequest;
import com.management_system.dto.response.ProjectResponse;
import com.management_system.dto.response.PageResponse;

public interface IProjectService {
    PageResponse<ProjectResponse> getPage(String status, int page, int size);

    PageResponse<ProjectResponse> getMyProjects(UUID userId, int page, int size);

    ProjectResponse get(UUID id);

    ProjectResponse create(ProjectRequest request);

    ProjectResponse createFuture(ProjectRequest request);

    ProjectResponse update(UUID id, ProjectRequest request);

    void delete(UUID id);

    boolean existsByNameExcludingId(String projectName, UUID excludeId);
}
