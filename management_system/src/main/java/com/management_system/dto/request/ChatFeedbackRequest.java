package com.management_system.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatFeedbackRequest {

    @NotNull(message = "Conversation ID is required")
    private UUID conversationId;

    @NotNull(message = "Message ID is required")
    private UUID messageId;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private Boolean isHelpful;

    private String feedbackText;

    private String issueType; // INACCURATE, IRRELEVANT, INCOMPLETE, RUDE, OTHER
}
