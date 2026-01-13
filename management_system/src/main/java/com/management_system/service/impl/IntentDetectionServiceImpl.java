package com.management_system.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.management_system.service.inter.IIntentDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentDetectionServiceImpl implements IIntentDetectionService {

    // Intent keywords mapping
    private static final Map<String, String[]> INTENT_KEYWORDS = new HashMap<>();

    static {
        // PRICING_INQUIRY
        INTENT_KEYWORDS.put("PRICING_INQUIRY", new String[] {
                "giá", "chi phí", "budget", "cost", "price", "pricing", "expensive", "cheap",
                "phí", "tiền", "đắt", "rẻ", "bao nhiêu", "how much", "estimate"
        });

        // TECH_RECOMMENDATION
        INTENT_KEYWORDS.put("TECH_RECOMMENDATION", new String[] {
                "công nghệ", "technology", "tech stack", "framework", "library", "ngôn ngữ",
                "language", "database", "backend", "frontend", "nên dùng", "recommend", "suggest",
                "gợi ý", "tư vấn", "advise", "best", "tốt nhất", "phù hợp"
        });

        // PROJECT_TIMELINE
        INTENT_KEYWORDS.put("PROJECT_TIMELINE", new String[] {
                "thời gian", "timeline", "khi nào", "when", "bao lâu", "how long", "duration",
                "deadline", "schedule", "hoàn thành", "complete", "finish", "delivery",
                "giao hàng", "ngày", "tháng", "month", "week", "tuần"
        });

        // FEATURE_REQUEST
        INTENT_KEYWORDS.put("FEATURE_REQUEST", new String[] {
                "tính năng", "feature", "chức năng", "function", "module", "có thể", "can",
                "làm được", "able to", "hỗ trợ", "support", "integrate", "tích hợp",
                "cần", "need", "want", "muốn", "yêu cầu", "require"
        });

        // GENERAL_INFO
        INTENT_KEYWORDS.put("GENERAL_INFO", new String[] {
                "thông tin", "information", "giới thiệu", "introduce", "about", "về",
                "dịch vụ", "service", "công ty", "company", "team", "đội ngũ",
                "liên hệ", "contact", "email", "phone"
        });
    }

    @Override
    public String detectIntent(String message, String locale) {
        if (message == null || message.isBlank()) {
            return "GENERAL_INFO";
        }

        String normalizedMessage = message.toLowerCase();

        // Score each intent
        Map<String, Integer> intentScores = new HashMap<>();
        for (Map.Entry<String, String[]> entry : INTENT_KEYWORDS.entrySet()) {
            String intent = entry.getKey();
            String[] keywords = entry.getValue();
            int score = 0;

            for (String keyword : keywords) {
                if (normalizedMessage.contains(keyword.toLowerCase())) {
                    score += 1;
                }
            }

            intentScores.put(intent, score);
        }

        // Find intent with highest score
        String detectedIntent = "GENERAL_INFO";
        int maxScore = 0;

        for (Map.Entry<String, Integer> entry : intentScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                detectedIntent = entry.getKey();
            }
        }

        log.debug("Detected intent: {} with score: {} for message: {}",
                detectedIntent, maxScore, message.substring(0, Math.min(50, message.length())));

        return detectedIntent;
    }

    @Override
    public Map<String, Object> extractEntities(String message, String intent) {
        Map<String, Object> entities = new HashMap<>();

        if (message == null || message.isBlank()) {
            return entities;
        }

        String normalizedMessage = message.toLowerCase();

        // Extract budget
        String budget = extractBudget(normalizedMessage);
        if (budget != null) {
            entities.put("budget", budget);
        }

        // Extract timeline
        String timeline = extractTimeline(normalizedMessage);
        if (timeline != null) {
            entities.put("timeline", timeline);
        }

        // Extract project type
        String projectType = extractProjectType(normalizedMessage);
        if (projectType != null) {
            entities.put("project_type", projectType);
        }

        // Extract technology mentions
        String[] techMentions = extractTechnologyMentions(normalizedMessage);
        if (techMentions.length > 0) {
            entities.put("mentioned_technologies", techMentions);
        }

        return entities;
    }

    @Override
    public double calculateConfidence(String message, String intent) {
        if (message == null || message.isBlank()) {
            return 0.5;
        }

        String normalizedMessage = message.toLowerCase();
        String[] keywords = INTENT_KEYWORDS.get(intent);

        if (keywords == null) {
            return 0.5;
        }

        int matchCount = 0;
        for (String keyword : keywords) {
            if (normalizedMessage.contains(keyword.toLowerCase())) {
                matchCount++;
            }
        }

        // Calculate confidence based on keyword matches
        double confidence = Math.min(0.5 + (matchCount * 0.1), 0.95);

        return confidence;
    }

    // Helper methods for entity extraction

    private String extractBudget(String message) {
        // Pattern for Vietnamese: "50 triệu", "100 triệu", "50M", "100K"
        Pattern viPattern = Pattern.compile("(\\d+)\\s*(triệu|tỷ|k|m)", Pattern.CASE_INSENSITIVE);
        Matcher viMatcher = viPattern.matcher(message);
        if (viMatcher.find()) {
            return viMatcher.group(1) + viMatcher.group(2);
        }

        // Pattern for English: "$50M", "$100K", "50 million"
        Pattern enPattern = Pattern.compile("\\$?(\\d+)\\s*(million|m|thousand|k)", Pattern.CASE_INSENSITIVE);
        Matcher enMatcher = enPattern.matcher(message);
        if (enMatcher.find()) {
            return enMatcher.group(1) + enMatcher.group(2);
        }

        return null;
    }

    private String extractTimeline(String message) {
        // Pattern for timeline: "3 tháng", "6 months", "2 weeks"
        Pattern pattern = Pattern.compile("(\\d+)\\s*(tháng|tuần|ngày|months?|weeks?|days?)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1) + " " + matcher.group(2);
        }

        return null;
    }

    private String extractProjectType(String message) {
        String[] projectTypes = {
                "website", "web app", "mobile app", "ứng dụng di động",
                "e-commerce", "thương mại điện tử", "cms", "crm", "erp",
                "api", "backend", "frontend", "platform", "nền tảng"
        };

        for (String type : projectTypes) {
            if (message.contains(type)) {
                return type;
            }
        }

        return null;
    }

    private String[] extractTechnologyMentions(String message) {
        String[] technologies = {
                "react", "vue", "angular", "node", "java", "spring boot",
                "python", "django", "flask", "postgresql", "mysql", "mongodb",
                "redis", "docker", "kubernetes", "aws", "azure", "gcp"
        };

        return java.util.Arrays.stream(technologies)
                .filter(tech -> message.contains(tech))
                .toArray(String[]::new);
    }
}
