package com.management_system.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_conversations")
@Getter
@Setter
public class ChatConversation extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 10, nullable = false)
    private String locale = "en";

    @Column(length = 255)
    private String title;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;
}
