package com.management_system.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.request.TeamMemberRequest;
import com.management_system.dto.request.TeamRequest;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.TeamResponse;
import com.management_system.service.inter.ITeamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final ITeamService teamService;

    @GetMapping
    public ResponseEntity<PageResponse<TeamResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(teamService.getPage(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<TeamResponse> update(@PathVariable UUID id, @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.update(id, request));
    }

    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<TeamResponse> addMember(@PathVariable UUID teamId, @RequestBody TeamMemberRequest request) {
        return ResponseEntity.ok(teamService.addMember(teamId, request));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<Void> removeMember(@PathVariable UUID teamId, @PathVariable UUID userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }
}
