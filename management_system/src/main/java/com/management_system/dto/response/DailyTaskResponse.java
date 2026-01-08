package com.management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.management_system.enums.TaskPriority;
import com.management_system.enums.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTaskResponse {
    private UUID id;
    private UUID projectId;
    private UUID assignedTo;
    private String assignedToUserName;
    private String title;
    private String description;
    private LocalDate taskDate;
    private TaskPriority priority;
    private Double estimatedHours;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
