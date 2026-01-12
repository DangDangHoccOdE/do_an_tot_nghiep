package com.management_system.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatConversationResponse {
    private UUID id;
    private String title;
    private String locale;
    private OffsetDateTime lastMessageAt;
    private Integer messageCount;
}
