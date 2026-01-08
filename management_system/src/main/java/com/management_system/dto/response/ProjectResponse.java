package com.management_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.management_system.entity.enums.ProjectStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectResponse {
    private UUID id;
    private UUID clientId;
    private UUID teamId;
    private String projectName;
    private String description;
    private BigDecimal budgetEstimated;
    private String currencyUnit;
    private Integer timelineEstimated;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ProjectMemberResponse> members;
}
