package com.management_system.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.management_system.entity.enums.ProjectStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {
    private UUID clientId;
    private UUID teamId;
    private String projectName;
    private String description;
    private BigDecimal budgetEstimated;
    private Integer timelineEstimated;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}
