package com.management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDailyMetricsResponse {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private LocalDate reportDate;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer blockedTasks;
    private Integer pendingTasks;
    private BigDecimal completionRate;
    private BigDecimal totalEstimatedHours;
    private BigDecimal totalCompletedHours;
    private BigDecimal teamProductivityIndex;
    private Integer teamMembersAssigned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
