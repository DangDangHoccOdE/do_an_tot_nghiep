package com.management_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.management_system.entity.AiKnowledgeChunk;

@Repository
public interface AiKnowledgeChunkRepository extends BaseRepository<AiKnowledgeChunk, UUID> {

    @Query(value = "SELECT k FROM AiKnowledgeChunk k WHERE k.deleteFlag = false AND k.language = :language ORDER BY k.updatedAt DESC")
    List<AiKnowledgeChunk> findRecentByLanguage(String language);

    @Query(value = "SELECT k FROM AiKnowledgeChunk k WHERE k.deleteFlag = false ORDER BY k.updatedAt DESC")
    List<AiKnowledgeChunk> findAllActive();
}
