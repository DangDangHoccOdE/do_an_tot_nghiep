package com.management_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.management_system.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReportResponse {
    private UUID id;
    private UUID dailyTaskId;
    private UUID reportedBy;
    private String reportedByUserName;
    private LocalDateTime reportedAt;
    private ReportStatus status;
    private Integer completedHours;
    private Integer completionPercentage;
    private String notes;
    private String evidenceLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
