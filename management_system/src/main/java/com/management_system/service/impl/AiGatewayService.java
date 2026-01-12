package com.management_system.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@Service
public class AiGatewayService {

    private final WebClient webClient;

    @Value("${ai.provider:gemini}")
    private String provider;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.0-pro}")
    private String geminiModel;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${ai.timeout-ms:20000}")
    private long timeoutMs;

    public AiGatewayService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public double[] embed(String text, String locale) {
        ensureConfigured();
        if ("openai".equalsIgnoreCase(provider)) {
            return embedOpenAi(text);
        }
        return embedGemini(text, locale);
    }

    public String chat(String systemPrompt, String userMessage, List<String> contexts, List<String> history,
            String locale) {
        ensureConfigured();
        if ("openai".equalsIgnoreCase(provider)) {
            return chatOpenAi(systemPrompt, userMessage, contexts, history, locale);
        }
        return chatGemini(systemPrompt, userMessage, contexts, history, locale);
    }

    public void ensureConfigured() {
        if ("openai".equalsIgnoreCase(provider)) {
            if (openAiApiKey == null || openAiApiKey.isBlank()) {
                throw new IllegalStateException("OpenAI API key is missing. Set ai.openai.api-key");
            }
        } else {
            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                throw new IllegalStateException("Gemini API key is missing. Set ai.gemini.api-key");
            }
        }
    }

    public String getActiveProvider() {
        return "openai".equalsIgnoreCase(provider) ? "openai" : "gemini";
    }

    public String getModelName() {
        return "openai".equalsIgnoreCase(provider) ? openAiModel : geminiModel;
    }

    private double[] embedGemini(String text, String locale) {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key="
                + geminiApiKey;

        Map<String, Object> body = Map.of(
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text))));

        Map<?, ?> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.error(new RuntimeException(ex.getResponseBodyAsString())))
                .block();

        if (response == null || !response.containsKey("embedding")) {
            throw new IllegalStateException("Gemini embedding response is empty");
        }

        Map<?, ?> embedding = (Map<?, ?>) response.get("embedding");
        List<?> values = (List<?>) embedding.get("values");

        return values.stream()
                .mapToDouble(v -> ((Number) v).doubleValue())
                .toArray();
    }

    private double[] embedOpenAi(String text) {
        String url = "https://api.openai.com/v1/embeddings";
        Map<String, Object> body = new HashMap<>();
        body.put("model", "text-embedding-3-small");
        body.put("input", text);
        Map<?, ?> response = webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.error(new RuntimeException(ex.getResponseBodyAsString())))
                .block();
        if (response == null || !response.containsKey("data")) {
            throw new IllegalStateException("OpenAI embedding response is empty");
        }
        List<?> data = (List<?>) response.get("data");
        if (data.isEmpty())
            throw new IllegalStateException("OpenAI embedding data empty");
        Map<?, ?> first = (Map<?, ?>) data.get(0);
        List<?> values = (List<?>) first.get("embedding");
        return values.stream().mapToDouble(v -> ((Number) v).doubleValue()).toArray();
    }

    private String chatGemini(String systemPrompt, String userMessage, List<String> contexts, List<String> history,
            String locale) {
        String url = "https://generativelanguage.googleapis.com/v1/models/"
                + geminiModel
                + ":generateContent?key="
                + geminiApiKey;
        StringBuilder prompt = new StringBuilder();
        prompt.append(systemPrompt).append("\n\n");
        if (contexts != null && !contexts.isEmpty()) {
            prompt.append("Context:\n");
            for (String c : contexts) {
                prompt.append("- ").append(c).append("\n");
            }
            prompt.append("\n");
        }
        if (history != null && !history.isEmpty()) {
            prompt.append("Short chat history:\n");
            for (String h : history) {
                prompt.append(h).append("\n");
            }
            prompt.append("\n");
        }
        prompt.append("User (" + (locale == null ? "en" : locale) + "): ").append(userMessage);

        Map<String, Object> part = Map.of("text", prompt.toString());
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(part))),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "topK", 32,
                        "topP", 0.9,
                        "maxOutputTokens", 900));

        Map<?, ?> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.error(new RuntimeException(ex.getResponseBodyAsString())))
                .block();
        if (response == null)
            throw new IllegalStateException("Gemini response is empty");
        List<?> candidates = (List<?>) response.get("candidates");
        if (candidates == null || candidates.isEmpty())
            throw new IllegalStateException("Gemini returned no candidates");
        Map<?, ?> cand = (Map<?, ?>) candidates.get(0);
        Map<?, ?> content = (Map<?, ?>) cand.get("content");
        List<?> parts = content == null ? null : (List<?>) content.get("parts");
        if (parts == null || parts.isEmpty())
            throw new IllegalStateException("Gemini parts missing");
        Map<?, ?> first = (Map<?, ?>) parts.get(0);
        Object text = first.get("text");
        return text == null ? "" : text.toString();
    }

    private String chatOpenAi(String systemPrompt, String userMessage, List<String> contexts, List<String> history,
            String locale) {
        StringBuilder contextBuilder = new StringBuilder();
        if (contexts != null && !contexts.isEmpty()) {
            contextBuilder.append("Context:\n");
            contexts.forEach(c -> contextBuilder.append("- ").append(c).append("\n"));
        }
        if (history != null && !history.isEmpty()) {
            contextBuilder.append("\nShort chat history:\n");
            history.forEach(h -> contextBuilder.append(h).append("\n"));
        }

        Map<String, Object> body = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content",
                                contextBuilder + "\nUser (" + (locale == null ? "en" : locale) + "): " + userMessage)),
                "temperature", 0.4,
                "max_tokens", 900);

        Map<?, ?> response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.error(new RuntimeException(ex.getResponseBodyAsString())))
                .block();

        if (response == null || !response.containsKey("choices")) {
            throw new IllegalStateException("OpenAI response is empty");
        }
        List<?> choices = (List<?>) response.get("choices");
        if (choices.isEmpty())
            throw new IllegalStateException("OpenAI returned no choices");
        Map<?, ?> first = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) first.get("message");
        Object text = message == null ? null : message.get("content");
        return text == null ? "" : text.toString();
    }
}
