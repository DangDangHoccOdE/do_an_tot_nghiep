package com.management_system.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.constant.ErrorCode;
import com.management_system.constant.MessageKey;
import com.management_system.dto.request.DailyTaskRequest;
import com.management_system.dto.response.DailyTaskResponse;
import com.management_system.entity.User;
import com.management_system.enums.TaskStatus;
import com.management_system.repository.UserRepository;
import com.management_system.service.DailyTaskService;
import com.management_system.service.inter.IUserService;
import com.management_system.utils.MessageUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/daily-tasks")
@RequiredArgsConstructor
public class DailyTaskController {
    private final DailyTaskService dailyTaskService;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DailyTaskResponse> createDailyTask(@Valid @RequestBody DailyTaskRequest request) {
        DailyTaskResponse response = dailyTaskService.createDailyTask(request, UUID.fromString(getCurrentUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DailyTaskResponse> updateDailyTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody DailyTaskRequest request) {
        DailyTaskResponse response = dailyTaskService.updateDailyTask(taskId, request,
                UUID.fromString(getCurrentUserId()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DailyTaskResponse> getDailyTask(@PathVariable UUID taskId) {
        DailyTaskResponse response = dailyTaskService.getDailyTask(taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyTaskResponse>> getTasksByProject(@PathVariable UUID projectId) {
        List<DailyTaskResponse> tasks = dailyTaskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}/date/{taskDate}")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyTaskResponse>> getTasksByProjectAndDate(
            @PathVariable UUID projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate taskDate) {
        List<DailyTaskResponse> tasks = dailyTaskService.getTasksByProjectAndDate(projectId, taskDate);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyTaskResponse>> getTasksByAssignedUser(@PathVariable UUID userId) {
        List<DailyTaskResponse> tasks = dailyTaskService.getTasksByAssignedUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user/{userId}/date/{taskDate}")
    @PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyTaskResponse>> getTasksByAssignedUserAndDate(
            @PathVariable UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate taskDate) {
        List<DailyTaskResponse> tasks = dailyTaskService.getTasksByAssignedUserAndDate(userId, taskDate);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}/range")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyTaskResponse>> getTasksByDateRange(
            @PathVariable UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyTaskResponse> tasks = dailyTaskService.getTasksByDateRange(projectId, startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyTaskResponse>> getTasksByStatus(
            @PathVariable UUID projectId,
            @PathVariable TaskStatus status) {
        List<DailyTaskResponse> tasks = dailyTaskService.getTasksByStatus(projectId, status);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteDailyTask(@PathVariable UUID taskId) {
        dailyTaskService.deleteDailyTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}/date/{taskDate}/count")
    @PreAuthorize("hasAuthority('ROLE_PM') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Integer> getTaskCount(
            @PathVariable UUID projectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate taskDate) {
        int count = dailyTaskService.getTaskCountByProjectAndDate(projectId, taskDate);
        return ResponseEntity.ok(count);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByEmailAndDeleteFlagFalse(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.format(ErrorCode.ERR003, MessageKey.USER, LocaleContextHolder.getLocale())));
        return user.getId().toString();
    }
}
