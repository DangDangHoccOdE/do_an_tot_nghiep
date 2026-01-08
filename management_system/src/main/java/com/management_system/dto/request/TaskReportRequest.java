package com.management_system.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.management_system.enums.ReportStatus;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReportRequest {
    @NotNull(message = "Daily task ID is required")
    private UUID dailyTaskId;

    @NotNull(message = "Report status is required")
    private ReportStatus status;

    @Min(value = 0, message = "Completed hours must be non-negative")
    @Max(value = 24, message = "Completed hours cannot exceed 24")
    private Integer completedHours;

    @Min(value = 0, message = "Completion percentage must be non-negative")
    @Max(value = 100, message = "Completion percentage cannot exceed 100")
    private Integer completionPercentage;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Size(max = 500, message = "Evidence link must not exceed 500 characters")
    private String evidenceLink;
}
