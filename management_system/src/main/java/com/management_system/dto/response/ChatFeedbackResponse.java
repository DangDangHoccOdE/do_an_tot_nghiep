package com.management_system.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatFeedbackResponse {
    private UUID id;
    private UUID conversationId;
    private UUID messageId;
    private Integer rating;
    private Boolean isHelpful;
    private String feedbackText;
    private String issueType;
    private OffsetDateTime createdAt;
}
