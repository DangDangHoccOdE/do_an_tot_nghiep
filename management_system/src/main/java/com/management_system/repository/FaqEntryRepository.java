package com.management_system.repository;

import com.management_system.entity.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqEntryRepository extends JpaRepository<FaqEntry, UUID> {

    @Query("SELECT f FROM FaqEntry f WHERE f.deleteFlag = false AND f.language = :language ORDER BY f.helpfulCount DESC, f.viewCount DESC")
    List<FaqEntry> findByLanguageOrderByPopularity(@Param("language") String language);

    @Query("SELECT f FROM FaqEntry f WHERE f.deleteFlag = false AND f.category = :category AND f.language = :language")
    List<FaqEntry> findByCategoryAndLanguage(@Param("category") String category, @Param("language") String language);

    @Query("SELECT f FROM FaqEntry f WHERE f.deleteFlag = false AND f.embedding IS NOT NULL")
    List<FaqEntry> findAllWithEmbeddings();
}
