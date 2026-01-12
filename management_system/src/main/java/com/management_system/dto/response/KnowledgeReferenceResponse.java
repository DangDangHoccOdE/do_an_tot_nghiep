package com.management_system.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KnowledgeReferenceResponse {
    private String title;
    private String snippet;
    private String source;
    private String sourceId;
    private double score;
}
