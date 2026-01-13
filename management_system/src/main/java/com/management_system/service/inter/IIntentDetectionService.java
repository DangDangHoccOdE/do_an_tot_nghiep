package com.management_system.service.inter;

import java.util.Map;

import com.management_system.dto.response.ChatIntentResponse;

public interface IIntentDetectionService {

    /**
     * Detect user intent from message
     * 
     * @param message User message
     * @param locale  Language locale
     * @return Detected intent (PRICING_INQUIRY, TECH_RECOMMENDATION, etc.)
     */
    String detectIntent(String message, String locale);

    /**
     * Extract entities from message based on detected intent
     * 
     * @param message User message
     * @param intent  Detected intent
     * @return Map of extracted entities (budget, timeline, tech, etc.)
     */
    Map<String, Object> extractEntities(String message, String intent);

    /**
     * Calculate confidence score for detected intent
     * 
     * @param message User message
     * @param intent  Detected intent
     * @return Confidence score (0.0 - 1.0)
     */
    double calculateConfidence(String message, String intent);
}
