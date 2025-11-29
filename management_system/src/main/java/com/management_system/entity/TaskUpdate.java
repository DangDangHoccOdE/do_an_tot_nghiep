package com.management_system.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "task_updates")
@Getter
@Setter
public class TaskUpdate extends BaseEntity {
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "updated_by_user_id", nullable = false)
    private UUID updatedByUserId;

    @Column(columnDefinition = "TEXT")
    private String updatedText;

    @Column(name = "progress_percent")
    private Integer progressPercent;
}
