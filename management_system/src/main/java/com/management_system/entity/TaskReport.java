package com.management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.management_system.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_reports", indexes = {
        @Index(name = "idx_task_reports_daily_task", columnList = "daily_task_id"),
        @Index(name = "idx_task_reports_reported_by", columnList = "reported_by"),
        @Index(name = "idx_task_reports_reported_at", columnList = "reported_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID dailyTaskId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID reportedBy; // User ID

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column
    private Integer completedHours;

    @Column(columnDefinition = "INTEGER CHECK (completion_percentage >= 0 AND completion_percentage <= 100)")
    private Integer completionPercentage = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String evidenceLink; // URL to file/attachment

    @Column(columnDefinition = "boolean default false")
    private Boolean deleteFlag = false;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        deleteFlag = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
