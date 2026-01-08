package com.management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_daily_metrics", indexes = {
        @Index(name = "idx_metrics_project_id", columnList = "project_id"),
        @Index(name = "idx_metrics_report_date", columnList = "report_date"),
        @Index(name = "idx_metrics_project_date", columnList = "project_id, report_date", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDailyMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID projectId;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK (total_tasks >= 0)")
    private Integer totalTasks = 0;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK (completed_tasks >= 0)")
    private Integer completedTasks = 0;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK (in_progress_tasks >= 0)")
    private Integer inProgressTasks = 0;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK (blocked_tasks >= 0)")
    private Integer blockedTasks = 0;

    @Column(nullable = false, columnDefinition = "INTEGER CHECK (pending_tasks >= 0)")
    private Integer pendingTasks = 0;

    @Column(columnDefinition = "DECIMAL(5,2) CHECK (completion_rate >= 0 AND completion_rate <= 100)")
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalEstimatedHours = BigDecimal.ZERO;

    @Column(columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalCompletedHours = BigDecimal.ZERO;

    @Column(columnDefinition = "DECIMAL(5,2)")
    private BigDecimal teamProductivityIndex = BigDecimal.ZERO; // Ratio of completed to estimated

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer teamMembersAssigned = 0;

    @Column(columnDefinition = "boolean default false")
    private Boolean deleteFlag = false;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        deleteFlag = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
