package com.management_system.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.management_system.entity.enums.TaskStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskResponse {
    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private UUID assignedToUserId;
    private LocalDate startDate;
    private LocalDate dueDate;
}
