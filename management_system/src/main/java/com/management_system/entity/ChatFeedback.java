package com.management_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.management_system.entity.enums.IssueType;

@Entity
@Table(name = "chat_feedback", indexes = {
        @Index(name = "idx_chat_feedback_conversation", columnList = "conversation_id"),
        @Index(name = "idx_chat_feedback_rating", columnList = "rating"),
        @Index(name = "idx_chat_feedback_created_at", columnList = "created_at")
})
@Getter
@Setter
public class ChatFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
    private UUID conversationId;

    @Column(name = "message_id", nullable = false, columnDefinition = "uuid")
    private UUID messageId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column
    private Integer rating; // 1-5

    @Column(name = "is_helpful")
    private Boolean isHelpful;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", columnDefinition = "issue_type_enum")
    public IssueType issueType;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
