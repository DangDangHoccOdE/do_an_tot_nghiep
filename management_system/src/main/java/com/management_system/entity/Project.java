package com.management_system.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.management_system.entity.enums.ProjectStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project extends BaseEntity {
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "project_name", length = 200, nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "budget_estimated", precision = 18, scale = 2)
    private BigDecimal budgetEstimated;

    @Column(name = "timeline_estimated")
    private Integer timelineEstimated; // in days

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status = ProjectStatus.PENDING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
