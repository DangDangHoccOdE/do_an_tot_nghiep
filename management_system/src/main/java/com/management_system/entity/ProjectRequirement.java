package com.management_system.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class ProjectRequirement extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(columnDefinition = "TEXT")
    private String requirementText;

    @Column(name = "ai_estimate_cost", precision = 18, scale = 2)
    private BigDecimal aiEstimateCost;

    @Column(name = "ai_estimate_time")
    private Integer aiEstimateTime; // in days
}
