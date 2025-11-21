package com.management_system.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class ProjectAssignment {
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssignmentStatus status = AssignmentStatus.TODO;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
