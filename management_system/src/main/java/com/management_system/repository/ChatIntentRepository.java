package com.management_system.repository;

import com.management_system.entity.ChatIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatIntentRepository extends JpaRepository<ChatIntent, UUID> {

    List<ChatIntent> findByConversationId(UUID conversationId);

    @Query("SELECT i.detectedIntent, COUNT(i) FROM ChatIntent i GROUP BY i.detectedIntent ORDER BY COUNT(i) DESC")
    List<Object[]> getIntentStatistics();

    @Query("SELECT i FROM ChatIntent i WHERE i.conversationId = :conversationId ORDER BY i.createdAt DESC")
    List<ChatIntent> findByConversationIdOrderByCreatedAtDesc(@Param("conversationId") UUID conversationId);
}
