package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.management_system.entity.ChatConversation;

@Repository
public interface ChatConversationRepository extends BaseRepository<ChatConversation, UUID> {

    Optional<ChatConversation> findByIdAndDeleteFlagFalse(UUID id);

    @Query("SELECT c FROM ChatConversation c WHERE c.deleteFlag = false ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    Page<ChatConversation> findRecent(Pageable pageable);
}
