package com.management_system.dto.request;

import com.management_system.enums.KnowledgeSource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatIngestRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String language = "en";

    @NotNull
    private KnowledgeSource source;

    private String sourceId;
}
