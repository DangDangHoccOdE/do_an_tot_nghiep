package com.management_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.management_system.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends BaseRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE m.conversationId = :conversationId AND m.deleteFlag = false ORDER BY m.createdAt ASC")
    List<ChatMessage> findByConversationId(UUID conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversationId = :conversationId AND m.deleteFlag = false")
    long countByConversationId(UUID conversationId);
}
