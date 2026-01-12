package com.management_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "faq_entries", indexes = {
        @Index(name = "idx_faq_entries_category", columnList = "category"),
        @Index(name = "idx_faq_entries_language", columnList = "language"),
        @Index(name = "idx_faq_entries_delete_flag", columnList = "delete_flag")
})
@Getter
@Setter
public class FaqEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(length = 50, nullable = false)
    private String category = "GENERAL"; // PRICING, TECHNOLOGY, TIMELINE, PROCESS, GENERAL

    @Column(length = 10, nullable = false)
    private String language = "vi";

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "double precision[]")
    private Double[] embedding;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "delete_flag")
    private Boolean deleteFlag = false;
}
