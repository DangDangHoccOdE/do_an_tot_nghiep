package com.management_system.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatAskResponse {
    private UUID conversationId;
    private ChatMessageResponse reply;
    private List<KnowledgeReferenceResponse> references;
    private String provider;
    private String model;
}
