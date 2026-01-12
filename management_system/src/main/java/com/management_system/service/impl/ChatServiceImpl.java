package com.management_system.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.management_system.dto.request.ChatAskRequest;
import com.management_system.dto.request.ChatIngestRequest;
import com.management_system.dto.response.ChatAskResponse;
import com.management_system.dto.response.ChatConversationResponse;
import com.management_system.dto.response.ChatMessageResponse;
import com.management_system.dto.response.KnowledgeReferenceResponse;
import com.management_system.entity.AiKnowledgeChunk;
import com.management_system.entity.ChatConversation;
import com.management_system.entity.ChatMessage;
import com.management_system.entity.DailyTask;
import com.management_system.entity.FaqEntry;
import com.management_system.entity.Project;
import com.management_system.entity.Team;
import com.management_system.entity.TechnologyStack;
import com.management_system.enums.ChatMessageRole;
import com.management_system.repository.AiKnowledgeChunkRepository;
import com.management_system.repository.ChatConversationRepository;
import com.management_system.repository.ChatMessageRepository;
import com.management_system.repository.DailyTaskRepository;
import com.management_system.repository.FaqEntryRepository;
import com.management_system.repository.ProjectRepository;
import com.management_system.repository.TeamRepository;
import com.management_system.repository.TechnologyStackRepository;
import com.management_system.service.inter.IChatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiKnowledgeChunkRepository knowledgeRepository;
    private final AiGatewayService aiGatewayService;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final FaqEntryRepository faqEntryRepository;
    private final TechnologyStackRepository technologyStackRepository;

    private static final int MAX_HISTORY = 8;
    private static final int MAX_REFERENCES = 5;
    private static final double MIN_SCORE = 0.52;

    @Override
    @Transactional
    public ChatAskResponse ask(ChatAskRequest request, Locale locale, UUID userId) {
        String localeTag = locale != null ? locale.getLanguage()
                : (request.getLocale() == null ? "en" : request.getLocale());
        ChatConversation conversation = findOrCreateConversation(request.getConversationId(), localeTag, userId,
                request.getMessage());

        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversationId(conversation.getId());
        userMsg.setRole(ChatMessageRole.USER);
        userMsg.setContent(request.getMessage());
        userMsg.setCreatedAt(OffsetDateTime.now());
        chatMessageRepository.save(userMsg);

        // Embed query
        double[] queryEmbedding = aiGatewayService.embed(request.getMessage(), localeTag);

        // Retrieve knowledge
        List<AiKnowledgeChunk> candidates = getKnowledge(localeTag);
        List<ScoredChunk> relevant = rankBySimilarity(queryEmbedding, candidates).stream()
                .filter(sc -> sc.score() >= MIN_SCORE)
                .limit(MAX_REFERENCES)
                .collect(Collectors.toList());

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
    public List<KnowledgeReferenceResponse> syncDomainData(Locale locale) {
        String lang = locale != null ? locale.getLanguage() : "en";
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

    private List<AiKnowledgeChunk> getKnowledge(String locale) {
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

    private record ScoredChunk(AiKnowledgeChunk chunk, double score) {
    }
}
