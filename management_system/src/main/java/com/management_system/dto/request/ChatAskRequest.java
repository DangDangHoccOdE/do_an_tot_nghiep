package com.management_system.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatAskRequest {
    private UUID conversationId;

    @NotBlank
    private String message;

    private String locale;
}
