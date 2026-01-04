package com.management_system.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.request.ProjectRequest;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.ProjectResponse;
import com.management_system.entity.User;
import com.management_system.service.impl.UserSecurityServiceImpl;
import com.management_system.service.inter.IProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;
    private final UserSecurityServiceImpl userSecurityService;

    @GetMapping
    public ResponseEntity<PageResponse<ProjectResponse>> list(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(projectService.getPage(status, page, size));
    }

    @GetMapping("/my-projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userSecurityService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(projectService.getMyProjects(user.getId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.create(request));
    }

    @PostMapping("/future")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<ProjectResponse> createFuture(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.createFuture(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
