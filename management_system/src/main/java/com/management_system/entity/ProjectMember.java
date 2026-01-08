package com.management_system.entity;

import java.util.UUID;

import com.management_system.entity.enums.ITRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_user", columnNames = { "project_id", "user_id" }) })
@Getter
@Setter
public class ProjectMember extends BaseEntity {
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "it_role", length = 50)
    private ITRole itRole;
}
