package com.management_system.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponse {
    private UUID id;
    private String role;
    private String content;
    private OffsetDateTime createdAt;
}
