package com.management_system.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_files")
@Getter
@Setter
public class ProjectFile extends BaseEntity {
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "uploaded_by_user_id")
    private UUID uploadedByUserId;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private Instant uploadedAt;

}
