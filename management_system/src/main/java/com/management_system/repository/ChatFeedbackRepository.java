package com.management_system.repository;

import com.management_system.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, UUID> {

    List<ChatFeedback> findByConversationId(UUID conversationId);

    @Query("SELECT AVG(CAST(f.rating AS double)) FROM ChatFeedback f WHERE f.rating IS NOT NULL")
    Double getAverageRating();

    @Query("SELECT f FROM ChatFeedback f WHERE f.rating IS NOT NULL AND f.rating <= 2 ORDER BY f.createdAt DESC")
    List<ChatFeedback> findLowRatedFeedback();
}
