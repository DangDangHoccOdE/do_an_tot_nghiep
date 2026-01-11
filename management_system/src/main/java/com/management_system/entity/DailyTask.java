package com.management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.management_system.enums.TaskPriority;
import com.management_system.enums.TaskStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_tasks", indexes = {
        @Index(name = "idx_daily_tasks_project", columnList = "project_id"),
        @Index(name = "idx_daily_tasks_assigned_to", columnList = "assigned_to"),
        @Index(name = "idx_daily_tasks_date", columnList = "task_date"),
        @Index(name = "idx_daily_tasks_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID projectId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID assignedTo; // User ID

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate taskDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "task_priority")
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column
    private Integer estimatedHours;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "task_status")
    private TaskStatus status = TaskStatus.TODO;

    @Column(columnDefinition = "boolean default false")
    private Boolean deleteFlag = false;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "uuid")
    private UUID createdBy;

    @Column(columnDefinition = "uuid")
    private UUID updatedBy;

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
