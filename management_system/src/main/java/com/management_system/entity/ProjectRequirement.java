package com.management_system.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class ProjectRequirement extends BaseEntity {
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(columnDefinition = "TEXT")
    private String requirementText;

    @Column(name = "ai_estimate_cost", precision = 18, scale = 2)
    private BigDecimal aiEstimateCost;

    @Column(name = "ai_estimate_time")
    private Integer aiEstimateTime; // in days
}
