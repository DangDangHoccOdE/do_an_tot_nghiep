package com.management_system.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.management_system.entity.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private UUID projectId;
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
    private TaskStatus status;
    private UUID assignedToUserId;
    private LocalDate startDate;
    private LocalDate dueDate;
}
