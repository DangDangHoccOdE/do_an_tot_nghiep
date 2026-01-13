package com.management_system.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatIntentResponse {
    private UUID id;
    private UUID conversationId;
    private String detectedIntent;
    private Double confidenceScore;
    private Map<String, Object> extractedEntities;
    private OffsetDateTime createdAt;
}
