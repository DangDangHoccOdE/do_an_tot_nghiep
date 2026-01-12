package com.management_system.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_knowledge_chunks")
@Getter
@Setter
public class AiKnowledgeChunk extends BaseEntity {

    @Column(length = 50, nullable = false)
    private String source;

    @Column(name = "source_id", length = 100)
    private String sourceId;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 10, nullable = false)
    private String language = "en";

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "double precision[]", nullable = false)
    private Double[] embedding;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex = 0;

    @Column(columnDefinition = "jsonb")
    private String metadata;
}
