package com.management_system.service.inter;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

public interface IChatService {
    ChatAskResponse ask(ChatAskRequest request, Locale locale, UUID userId);

    ChatConversationResponse getConversation(UUID id);

    List<ChatMessageResponse> getMessages(UUID conversationId);

    List<ChatConversationResponse> getConversations(int page, int size);

    KnowledgeReferenceResponse ingest(ChatIngestRequest request);

    List<KnowledgeReferenceResponse> syncDomainData(Locale locale);

    // Feedback methods
    ChatFeedbackResponse saveFeedback(ChatFeedbackRequest request, UUID userId);

    ChatFeedbackStatisticsResponse getFeedbackStatistics();

    List<ChatFeedbackResponse> getLowRatedFeedbacks();

    // Intent methods
    List<ChatIntentResponse> getConversationIntents(UUID conversationId);
}
