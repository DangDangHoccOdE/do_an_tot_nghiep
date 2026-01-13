package com.management_system.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatFeedbackStatisticsResponse {
    private Double averageRating;
    private Long totalFeedbacks;
    private Long helpfulCount;
    private Long notHelpfulCount;
    private List<FeedbackIssue> topIssues;

    @Getter
    @Builder
    public static class FeedbackIssue {
        private String issueType;
        private Long count;
    }
}
