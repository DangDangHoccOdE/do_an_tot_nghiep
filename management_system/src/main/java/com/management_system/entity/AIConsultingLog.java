package com.management_system.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_consulting_logs")
@Getter
@Setter
public class AIConsultingLog extends BaseEntity {
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String aiResponse;

}
