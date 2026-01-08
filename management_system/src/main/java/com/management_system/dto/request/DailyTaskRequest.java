package com.management_system.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.management_system.enums.TaskPriority;
import com.management_system.enums.TaskStatus;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTaskRequest {
    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "Assigned to (User ID) is required")
    private UUID assignedTo;

    @NotBlank(message = "Task title is required")
    @Size(min = 5, max = 255, message = "Task title must be between 5 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Task date is required")
    @FutureOrPresent(message = "Task date must be today or in the future")
    private LocalDate taskDate;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Estimated hours is required")
    @DecimalMin(value = "0.5", message = "Estimated hours must be at least 0.5")
    @DecimalMax(value = "24", message = "Estimated hours cannot exceed 24")
    private Double estimatedHours;

    private TaskStatus status = TaskStatus.TODO;
}
