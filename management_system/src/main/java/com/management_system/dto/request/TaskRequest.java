package com.management_system.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.management_system.entity.enums.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private UUID assignedToUserId;
    private LocalDate startDate;
    private LocalDate dueDate;
}
