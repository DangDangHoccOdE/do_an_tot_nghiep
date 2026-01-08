package com.management_system.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.management_system.entity.enums.ProjectStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {
    @NotNull(message = "Client is required")
    private UUID clientId;

    private UUID teamId;

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must be at most 200 characters")
    private String projectName;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget must be zero or positive")
    private BigDecimal budgetEstimated;

    private String currencyUnit = "VND";

    @Positive(message = "Timeline must be greater than zero")
    private Integer timelineEstimated;

    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<UUID> memberIds;
}
