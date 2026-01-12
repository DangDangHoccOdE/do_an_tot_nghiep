package com.management_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_intents", indexes = {
        @Index(name = "idx_chat_intents_conversation", columnList = "conversation_id"),
        @Index(name = "idx_chat_intents_intent", columnList = "detected_intent")
})
@Getter
@Setter
public class ChatIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "detected_intent", length = 50, nullable = false)
    private String detectedIntent; // PRICING_INQUIRY, TECH_RECOMMENDATION, PROJECT_TIMELINE, FEATURE_REQUEST,
                                   // GENERAL_INFO

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_entities", columnDefinition = "jsonb")
    private String extractedEntities; // e.g., {"budget": "50M", "timeline": "3 months", "type": "web"}

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
