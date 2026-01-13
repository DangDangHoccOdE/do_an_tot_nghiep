package com.management_system.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.management_system.entity.AiKnowledgeChunk;
import com.management_system.entity.ChatConversation;
import com.management_system.entity.ChatFeedback;
import com.management_system.entity.ChatIntent;
import com.management_system.entity.ChatMessage;
import com.management_system.entity.DailyTask;
import com.management_system.entity.FaqEntry;
import com.management_system.entity.Project;
import com.management_system.entity.Team;
import com.management_system.entity.TechnologyStack;
import com.management_system.entity.enums.IssueType;
import com.management_system.enums.ChatMessageRole;
import com.management_system.repository.AiKnowledgeChunkRepository;
import com.management_system.repository.ChatConversationRepository;
import com.management_system.repository.ChatFeedbackRepository;
import com.management_system.repository.ChatIntentRepository;
import com.management_system.repository.ChatMessageRepository;
import com.management_system.repository.DailyTaskRepository;
import com.management_system.repository.FaqEntryRepository;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.TeamRepository;
import com.management_system.repository.TechnologyStackRepository;
import com.management_system.service.inter.IChatService;
import com.management_system.service.inter.IIntentDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements IChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatIntentRepository chatIntentRepository;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final AiKnowledgeChunkRepository knowledgeRepository;
    private final AiGatewayService aiGatewayService;
    private final IIntentDetectionService intentDetectionService;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final FaqEntryRepository faqEntryRepository;
    private final TechnologyStackRepository technologyStackRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_HISTORY = 8;
    private static final int MAX_REFERENCES = 5;
    private static final double MIN_SCORE = 0.52;

    @Override
    @Transactional
    public ChatAskResponse ask(ChatAskRequest request, Locale locale, UUID userId) {
        String localeTag = locale != null ? locale.getLanguage()
                : (request.getLocale() == null ? "en" : request.getLocale());

        log.info("Chat request received: conversationId={}, locale={}, messageLength={}, userId={}",
                request.getConversationId(), localeTag, request.getMessage().length(), userId);

        try {
            ChatConversation conversation = findOrCreateConversation(request.getConversationId(), localeTag, userId,
                    request.getMessage());

            // Save user message
            ChatMessage userMsg = new ChatMessage();
            userMsg.setConversationId(conversation.getId());
            userMsg.setRole(ChatMessageRole.USER);
            userMsg.setContent(request.getMessage());
            userMsg.setCreatedAt(OffsetDateTime.now());
            chatMessageRepository.save(userMsg);

            // Detect intent and extract entities
            String detectedIntent = intentDetectionService.detectIntent(request.getMessage(), localeTag);
            Map<String, Object> extractedEntities = intentDetectionService.extractEntities(
                    request.getMessage(), detectedIntent);
            double confidenceScore = intentDetectionService.calculateConfidence(
                    request.getMessage(), detectedIntent);

            log.debug("Intent detected: intent={}, confidence={}, entities={}",
                    detectedIntent, confidenceScore, extractedEntities);

            // Save intent
            ChatIntent intent = new ChatIntent();
            intent.setConversationId(conversation.getId());
            intent.setDetectedIntent(detectedIntent);
            intent.setConfidenceScore(confidenceScore);
            try {
                intent.setExtractedEntities(objectMapper.writeValueAsString(extractedEntities));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize extracted entities", e);
                intent.setExtractedEntities("{}");
            }
            intent.setCreatedAt(OffsetDateTime.now());
            chatIntentRepository.save(intent);

            // Embed query
            double[] queryEmbedding = aiGatewayService.embed(request.getMessage(), localeTag);

            // Retrieve knowledge
            List<AiKnowledgeChunk> candidates = getKnowledge(localeTag);
            List<ScoredChunk> relevant = rankBySimilarity(queryEmbedding, candidates).stream()
                    .filter(sc -> sc.score() >= MIN_SCORE)
                    .limit(MAX_REFERENCES)
                    .collect(Collectors.toList());

            log.debug("Knowledge retrieval: totalCandidates={}, relevantChunks={}, minScore={}",
                    candidates.size(), relevant.size(), MIN_SCORE);

            // Build context and history
            List<String> contexts = relevant.stream()
                    .map(sc -> sc.chunk().getTitle() + ": " + truncate(sc.chunk().getContent(), 600))
                    .collect(Collectors.toList());

            List<ChatMessage> history = latestMessages(conversation.getId(), MAX_HISTORY);
            List<String> historyText = history.stream()
                    .map(m -> m.getRole().name().toLowerCase() + ": " + truncate(m.getContent(), 200))
                    .collect(Collectors.toList());

            String systemPrompt = buildSystemPrompt(localeTag);
            String answer = aiGatewayService.chat(systemPrompt, request.getMessage(), contexts, historyText, localeTag);

            // Save assistant message
            ChatMessage botMsg = new ChatMessage();
            botMsg.setConversationId(conversation.getId());
            botMsg.setRole(ChatMessageRole.ASSISTANT);
            botMsg.setContent(answer);
            botMsg.setCreatedAt(OffsetDateTime.now());
            chatMessageRepository.save(botMsg);

            conversation.setLastMessageAt(OffsetDateTime.now());
            conversationRepository.save(conversation);

            log.info("Chat response success: conversationId={}, provider={}, model={}, referencesCount={}",
                    conversation.getId(), aiGatewayService.getActiveProvider(),
                    aiGatewayService.getModelName(), relevant.size());

            return ChatAskResponse.builder()
                    .conversationId(conversation.getId())
                    .reply(ChatMessageResponse.builder()
                            .id(botMsg.getId())
                            .role(botMsg.getRole().name().toLowerCase())
                            .content(botMsg.getContent())
                            .createdAt(botMsg.getCreatedAt())
                            .build())
                    .references(relevant.stream().map(sc -> KnowledgeReferenceResponse.builder()
                            .title(sc.chunk().getTitle())
                            .snippet(truncate(sc.chunk().getContent(), 220))
                            .source(sc.chunk().getSource())
                            .sourceId(sc.chunk().getSourceId())
                            .score(sc.score())
                            .build()).collect(Collectors.toList()))
                    .provider(aiGatewayService.getActiveProvider())
                    .model(aiGatewayService.getModelName())
                    .build();
        } catch (Exception e) {
            log.error("Chat error: conversationId={}, error={}", request.getConversationId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ChatConversationResponse getConversation(UUID id) {
        ChatConversation c = conversationRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        long count = chatMessageRepository.countByConversationId(id);
        return ChatConversationResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .locale(c.getLocale())
                .lastMessageAt(c.getLastMessageAt())
                .messageCount((int) count)
                .build();
    }

    @Override
    public List<ChatMessageResponse> getMessages(UUID conversationId) {
        List<ChatMessage> items = chatMessageRepository.findByConversationId(conversationId);
        return items.stream().map(m -> ChatMessageResponse.builder()
                .id(m.getId())
                .role(m.getRole().name().toLowerCase())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<ChatConversationResponse> getConversations(int page, int size) {
        return conversationRepository.findRecent(org.springframework.data.domain.PageRequest.of(page, size)).stream()
                .map(c -> ChatConversationResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .locale(c.getLocale())
                        .lastMessageAt(c.getLastMessageAt())
                        .messageCount((int) chatMessageRepository.countByConversationId(c.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KnowledgeReferenceResponse ingest(ChatIngestRequest request) {
        double[] embedding = aiGatewayService.embed(request.getContent(), request.getLanguage());
        AiKnowledgeChunk chunk = new AiKnowledgeChunk();
        chunk.setContent(request.getContent());
        chunk.setTitle(request.getTitle());
        chunk.setLanguage(request.getLanguage());
        chunk.setSource(request.getSource().name());
        chunk.setSourceId(request.getSourceId());
        chunk.setEmbedding(toObjectArray(embedding));
        knowledgeRepository.save(chunk);
        return KnowledgeReferenceResponse.builder()
                .title(chunk.getTitle())
                .snippet(truncate(chunk.getContent(), 220))
                .source(chunk.getSource())
                .sourceId(chunk.getSourceId())
                .score(1.0)
                .build();
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "knowledgeCache", allEntries = true)
    public List<KnowledgeReferenceResponse> syncDomainData(Locale locale) {
        String lang = locale != null ? locale.getLanguage() : "en";
        log.info("Starting domain data sync for locale: {}", lang);
        List<KnowledgeReferenceResponse> results = new ArrayList<>();

        // 1. Company intro chunk
        String companyContent = "We provide software outsourcing, project management, and daily task tracking for clients."
                + " Projects, teams, tasks, revenue modules are available in the platform.";
        results.add(createKnowledgeChunk("Company Services", companyContent, "DOCUMENT", null, lang));

        // 2. Sync Projects data
        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            if (project.getDeleteFlag() == null || !project.getDeleteFlag()) {
                String projectContent = buildProjectContent(project);
                results.add(createKnowledgeChunk(
                        "Project: " + project.getProjectName(),
                        projectContent,
                        "PROJECT",
                        project.getId().toString(),
                        lang));
            }
        }

        // 3. Sync Teams data
        List<Team> teams = teamRepository.findAll();
        for (Team team : teams) {
            if (team.getDeleteFlag() == null || !team.getDeleteFlag()) {
                String teamContent = buildTeamContent(team);
                results.add(createKnowledgeChunk(
                        "Team: " + team.getName(),
                        teamContent,
                        "TEAM",
                        team.getId().toString(),
                        lang));
            }
        }

        // 4. Sync sample Tasks data (limit to recent 50 tasks)
        List<DailyTask> tasks = dailyTaskRepository.findAll().stream()
                .filter(t -> t.getDeleteFlag() == null || !t.getDeleteFlag())
                .sorted(Comparator.comparing(DailyTask::getCreatedAt).reversed())
                .limit(50)
                .collect(Collectors.toList());
        for (DailyTask task : tasks) {
            String taskContent = buildTaskContent(task);
            results.add(createKnowledgeChunk(
                    "Task: " + task.getTitle(),
                    taskContent,
                    "TASK",
                    task.getId().toString(),
                    lang));
        }

        // 5. Sync FAQ entries with embeddings
        List<FaqEntry> faqs = faqEntryRepository.findByLanguageOrderByPopularity(lang);
        for (FaqEntry faq : faqs) {
            String faqContent = "Q: " + faq.getQuestion() + " A: " + faq.getAnswer();
            results.add(createKnowledgeChunk(
                    "FAQ: " + faq.getCategory(),
                    faqContent,
                    "FAQ",
                    faq.getId().toString(),
                    lang));

            // Update FAQ embedding if not exists
            if (faq.getEmbedding() == null) {
                double[] embedding = aiGatewayService.embed(faqContent, lang);
                faq.setEmbedding(toObjectArray(embedding));
                faqEntryRepository.save(faq);
            }
        }

        // 6. Sync Technology Stacks with embeddings
        List<TechnologyStack> techStacks = technologyStackRepository.findAllOrderByPopularity();
        for (TechnologyStack tech : techStacks) {
            String techContent = buildTechnologyContent(tech);
            results.add(createKnowledgeChunk(
                    "Tech: " + tech.getName(),
                    techContent,
                    "DOCUMENT",
                    tech.getId().toString(),
                    lang));

            // Update tech stack embedding if not exists
            if (tech.getEmbedding() == null) {
                double[] embedding = aiGatewayService.embed(techContent, lang);
                tech.setEmbedding(toObjectArray(embedding));
                technologyStackRepository.save(tech);
            }
        }

        return results;
    }

    private KnowledgeReferenceResponse createKnowledgeChunk(String title, String content, String source,
            String sourceId, String lang) {
        double[] embedding = aiGatewayService.embed(content, lang);
        AiKnowledgeChunk chunk = new AiKnowledgeChunk();
        chunk.setTitle(title);
        chunk.setContent(content);
        chunk.setLanguage(lang);
        chunk.setSource(source);
        chunk.setSourceId(sourceId);
        chunk.setEmbedding(toObjectArray(embedding));
        knowledgeRepository.save(chunk);

        return KnowledgeReferenceResponse.builder()
                .title(title)
                .snippet(truncate(content, 200))
                .source(source)
                .sourceId(sourceId)
                .score(1.0)
                .build();
    }

    private String buildProjectContent(Project project) {
        StringBuilder content = new StringBuilder();
        content.append("Project Name: ").append(project.getProjectName()).append(". ");
        if (project.getDescription() != null) {
            content.append("Description: ").append(project.getDescription()).append(". ");
        }
        content.append("Status: ").append(project.getStatus()).append(". ");
        if (project.getBudgetEstimated() != null) {
            content.append("Budget Estimated: ").append(project.getBudgetEstimated())
                    .append(" ").append(project.getCurrencyUnit()).append(". ");
        }
        if (project.getTimelineEstimated() != null) {
            content.append("Timeline Estimated: ").append(project.getTimelineEstimated()).append(" days. ");
        }
        if (project.getStartDate() != null) {
            content.append("Start Date: ").append(project.getStartDate()).append(". ");
        }
        if (project.getEndDate() != null) {
            content.append("End Date: ").append(project.getEndDate()).append(". ");
        }
        return content.toString();
    }

    private String buildTeamContent(Team team) {
        StringBuilder content = new StringBuilder();
        content.append("Team Name: ").append(team.getName()).append(". ");
        if (team.getDescription() != null) {
            content.append("Description: ").append(team.getDescription()).append(". ");
        }
        // Add more team details as needed
        return content.toString();
    }

    private String buildTaskContent(DailyTask task) {
        StringBuilder content = new StringBuilder();
        content.append("Task: ").append(task.getTitle()).append(". ");
        if (task.getDescription() != null) {
            content.append("Description: ").append(task.getDescription()).append(". ");
        }
        content.append("Status: ").append(task.getStatus()).append(". ");
        if (task.getPriority() != null) {
            content.append("Priority: ").append(task.getPriority()).append(". ");
        }
        if (task.getEstimatedHours() != null) {
            content.append("Estimated Hours: ").append(task.getEstimatedHours()).append(". ");
        }
        if (task.getTaskDate() != null) {
            content.append("Date: ").append(task.getTaskDate()).append(". ");
        }
        return content.toString();
    }

    private String buildTechnologyContent(TechnologyStack tech) {
        StringBuilder content = new StringBuilder();
        content.append("Technology: ").append(tech.getName()).append(". ");
        if (tech.getDescription() != null) {
            content.append("Description: ").append(tech.getDescription()).append(". ");
        }
        content.append("Category: ").append(tech.getCategory()).append(". ");
        if (tech.getUseCases() != null && tech.getUseCases().length > 0) {
            content.append("Use Cases: ").append(String.join(", ", tech.getUseCases())).append(". ");
        }
        if (tech.getPros() != null) {
            content.append("Pros: ").append(tech.getPros()).append(". ");
        }
        if (tech.getCons() != null) {
            content.append("Cons: ").append(tech.getCons()).append(". ");
        }
        if (tech.getLearningCurve() != null) {
            content.append("Learning Curve: ").append(tech.getLearningCurve()).append(". ");
        }
        if (tech.getCostLevel() != null) {
            content.append("Cost: ").append(tech.getCostLevel()).append(". ");
        }
        content.append("Popularity: ").append(tech.getPopularityScore()).append("/100.");
        return content.toString();
    }

    private ChatConversation findOrCreateConversation(UUID conversationId, String locale, UUID userId, String message) {
        if (conversationId != null) {
            return conversationRepository.findByIdAndDeleteFlagFalse(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }
        ChatConversation c = new ChatConversation();
        c.setLocale(locale);
        c.setUserId(userId);
        c.setTitle(truncate(message, 60));
        c.setLastMessageAt(OffsetDateTime.now());
        return conversationRepository.save(c);
    }

    private List<ScoredChunk> rankBySimilarity(double[] query, List<AiKnowledgeChunk> candidates) {
        List<ScoredChunk> results = new ArrayList<>();
        for (AiKnowledgeChunk c : candidates) {
            double score = cosine(query, toPrimitiveArray(c.getEmbedding()));
            results.add(new ScoredChunk(c, score));
        }
        results.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return results;
    }

    private double cosine(double[] a, double[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0 || a.length != b.length)
            return 0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0 ? 0 : dot / denom;
    }

    @org.springframework.cache.annotation.Cacheable(value = "knowledgeCache", key = "#locale")
    private List<AiKnowledgeChunk> getKnowledge(String locale) {
        log.debug("Fetching knowledge from database for locale: {}", locale);
        List<AiKnowledgeChunk> localized = knowledgeRepository.findRecentByLanguage(locale);
        if (localized != null && !localized.isEmpty())
            return localized;
        return knowledgeRepository.findRecentByLanguage("en");
    }

    private List<ChatMessage> latestMessages(UUID conversationId, int limit) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationId(conversationId);
        int size = messages.size();
        if (size <= limit)
            return messages;
        return messages.subList(size - limit, size);
    }

    private String truncate(String text, int max) {
        if (text == null)
            return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private Double[] toObjectArray(double[] arr) {
        Double[] result = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private double[] toPrimitiveArray(Double[] arr) {
        if (arr == null)
            return new double[0];
        double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private String buildSystemPrompt(String locale) {
        return "You are an AI consultant for a Vietnamese software outsourcing company. "
                + "Always respond in the user's language (" + locale + ") with concise, helpful guidance. "
                + "Use the provided context from the database and knowledge base. "
                + "If information is missing, ask clarifying questions. "
                + "Offer project timelines, cost ranges, and suggest tech stacks (frontend, backend, database, devops). "
                + "Keep replies polite and encourage the user to share contact info for follow-up.";
    }

    // ==================== Feedback Methods ====================

    @Override
    @Transactional
    public ChatFeedbackResponse saveFeedback(ChatFeedbackRequest request, UUID userId) {
        log.info("Saving feedback: conversationId={}, messageId={}, rating={}, userId={}",
                request.getConversationId(), request.getMessageId(), request.getRating(), userId);

        ChatFeedback feedback = new ChatFeedback();
        feedback.setConversationId(request.getConversationId());
        feedback.setMessageId(request.getMessageId());
        feedback.setUserId(userId);
        feedback.setRating(request.getRating());
        feedback.setIsHelpful(request.getIsHelpful());
        feedback.setFeedbackText(request.getFeedbackText());

        // Convert string to enum if issueType is provided
        if (request.getIssueType() != null && !request.getIssueType().isBlank()) {
            try {
                feedback.setIssueType(IssueType.valueOf(request.getIssueType()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid issue type: {}", request.getIssueType());
            }
        }

        feedback.setCreatedAt(OffsetDateTime.now());

        feedback = chatFeedbackRepository.save(feedback);

        log.info("Feedback saved successfully: feedbackId={}", feedback.getId());

        return ChatFeedbackResponse.builder()
                .id(feedback.getId())
                .conversationId(feedback.getConversationId())
                .messageId(feedback.getMessageId())
                .rating(feedback.getRating())
                .isHelpful(feedback.getIsHelpful())
                .feedbackText(feedback.getFeedbackText())
                .issueType(feedback.getIssueType() != null ? feedback.getIssueType().name() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    @Override
    public ChatFeedbackStatisticsResponse getFeedbackStatistics() {
        Double avgRating = chatFeedbackRepository.getAverageRating();
        List<ChatFeedback> allFeedbacks = chatFeedbackRepository.findAll();

        long totalFeedbacks = allFeedbacks.size();
        long helpfulCount = allFeedbacks.stream()
                .filter(f -> f.getIsHelpful() != null && f.getIsHelpful())
                .count();
        long notHelpfulCount = allFeedbacks.stream()
                .filter(f -> f.getIsHelpful() != null && !f.getIsHelpful())
                .count();

        // Group by issue type (convert enum to string)
        Map<String, Long> issueGroups = allFeedbacks.stream()
                .filter(f -> f.getIssueType() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getIssueType().name(),
                        Collectors.counting()));

        List<ChatFeedbackStatisticsResponse.FeedbackIssue> topIssues = issueGroups.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> ChatFeedbackStatisticsResponse.FeedbackIssue.builder()
                        .issueType(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ChatFeedbackStatisticsResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalFeedbacks(totalFeedbacks)
                .helpfulCount(helpfulCount)
                .notHelpfulCount(notHelpfulCount)
                .topIssues(topIssues)
                .build();
    }

    @Override
    public List<ChatFeedbackResponse> getLowRatedFeedbacks() {
        List<ChatFeedback> lowRated = chatFeedbackRepository.findLowRatedFeedback();

        return lowRated.stream()
                .map(f -> ChatFeedbackResponse.builder()
                        .id(f.getId())
                        .conversationId(f.getConversationId())
                        .messageId(f.getMessageId())
                        .rating(f.getRating())
                        .isHelpful(f.getIsHelpful())
                        .feedbackText(f.getFeedbackText())
                        .issueType(f.getIssueType() != null ? f.getIssueType().name() : null)
                        .createdAt(f.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== Intent Methods ====================

    @Override
    public List<ChatIntentResponse> getConversationIntents(UUID conversationId) {
        List<ChatIntent> intents = chatIntentRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);

        return intents.stream()
                .map(i -> {
                    Map<String, Object> entities = new HashMap<>();
                    if (i.getExtractedEntities() != null && !i.getExtractedEntities().isBlank()) {
                        try {
                            entities = objectMapper.readValue(i.getExtractedEntities(), Map.class);
                        } catch (JsonProcessingException e) {
                            log.warn("Failed to deserialize extracted entities for intent: {}", i.getId(), e);
                        }
                    }

                    return ChatIntentResponse.builder()
                            .id(i.getId())
                            .conversationId(i.getConversationId())
                            .detectedIntent(i.getDetectedIntent())
                            .confidenceScore(i.getConfidenceScore())
                            .extractedEntities(entities)
                            .createdAt(i.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private record ScoredChunk(AiKnowledgeChunk chunk, double score) {
    }
}
