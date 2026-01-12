package com.management_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "technology_stacks", indexes = {
        @Index(name = "idx_technology_stacks_category", columnList = "category"),
        @Index(name = "idx_technology_stacks_delete_flag", columnList = "delete_flag")
})
@Getter
@Setter
public class TechnologyStack {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50, nullable = false)
    private String category; // FRONTEND, BACKEND, DATABASE, MOBILE, DEVOPS, TESTING, FULL_STACK

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] useCases;

    @Column(columnDefinition = "TEXT")
    private String pros;

    @Column(columnDefinition = "TEXT")
    private String cons;

    @Column(name = "learning_curve", length = 20)
    private String learningCurve; // LOW, MEDIUM, HIGH

    @Column(name = "cost_level", length = 20)
    private String costLevel; // FREE, AFFORDABLE, EXPENSIVE

    @Column(name = "popularity_score")
    private Integer popularityScore = 0; // 0-100

    @Column(length = 10)
    private String language = "vi";

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "double precision[]")
    private Double[] embedding;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "delete_flag")
    private Boolean deleteFlag = false;
}
