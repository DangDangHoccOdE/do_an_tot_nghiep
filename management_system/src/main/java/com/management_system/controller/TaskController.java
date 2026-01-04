package com.management_system.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.request.TaskRequest;
import com.management_system.dto.response.TaskResponse;
import com.management_system.service.inter.ITaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> listByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getByProject(projectId));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.get(id));
    }

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<TaskResponse> create(@PathVariable UUID projectId, @Valid @RequestBody TaskRequest request) {
        request.setProjectId(projectId);
        return ResponseEntity.ok(taskService.create(request));
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM','ROLE_STAFF')")
    public ResponseEntity<TaskResponse> update(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
