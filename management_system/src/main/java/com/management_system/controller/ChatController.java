package com.management_system.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.oauth2.UserPrincipal;
import com.management_system.dto.request.ChatAskRequest;
import com.management_system.dto.request.ChatFeedbackRequest;
import com.management_system.dto.request.ChatIngestRequest;
import com.management_system.dto.response.ChatAskResponse;
import com.management_system.dto.response.ChatConversationResponse;
import com.management_system.dto.response.ChatFeedbackResponse;
import com.management_system.dto.response.ChatFeedbackStatisticsResponse;
import com.management_system.dto.response.ChatIntentResponse;
import com.management_system.dto.response.ChatMessageResponse;
import com.management_system.dto.response.KnowledgeReferenceResponse;
import com.management_system.service.inter.IChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatAskResponse> ask(@Valid @RequestBody ChatAskRequest request,
            @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        ChatAskResponse response = chatService.ask(request, locale, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable UUID id) {
        return ResponseEntity.ok(chatService.getMessages(id));
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM','ROLE_STAFF')")
    public ResponseEntity<List<ChatConversationResponse>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getConversations(page, size));
    }

    @PostMapping("/ingest")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM','ROLE_STAFF')")
    public ResponseEntity<KnowledgeReferenceResponse> ingest(@Valid @RequestBody ChatIngestRequest request) {
        return ResponseEntity.ok(chatService.ingest(request));
    }

    @PostMapping("/sync-domain")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM','ROLE_STAFF')")
    public ResponseEntity<List<KnowledgeReferenceResponse>> sync(
            @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        return ResponseEntity.ok(chatService.syncDomainData(locale));
    }

    // ==================== Feedback Endpoints ====================

    @PostMapping("/feedback")
    public ResponseEntity<ChatFeedbackResponse> submitFeedback(
            @Valid @RequestBody ChatFeedbackRequest request,
            Authentication authentication) {
        UUID userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        ChatFeedbackResponse response = chatService.saveFeedback(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback/statistics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<ChatFeedbackStatisticsResponse> getFeedbackStatistics() {
        return ResponseEntity.ok(chatService.getFeedbackStatistics());
    }

    @GetMapping("/feedback/low-rated")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM')")
    public ResponseEntity<List<ChatFeedbackResponse>> getLowRatedFeedbacks() {
        return ResponseEntity.ok(chatService.getLowRatedFeedbacks());
    }

    // ==================== Intent Endpoints ====================

    @GetMapping("/conversations/{id}/intents")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_PM','ROLE_STAFF')")
    public ResponseEntity<List<ChatIntentResponse>> getConversationIntents(@PathVariable UUID id) {
        return ResponseEntity.ok(chatService.getConversationIntents(id));
    }
}
