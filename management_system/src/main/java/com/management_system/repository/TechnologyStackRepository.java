package com.management_system.repository;

import com.management_system.entity.TechnologyStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechnologyStackRepository extends JpaRepository<TechnologyStack, UUID> {

    @Query("SELECT t FROM TechnologyStack t WHERE t.deleteFlag = false AND t.category = :category ORDER BY t.popularityScore DESC")
    List<TechnologyStack> findByCategoryOrderByPopularity(@Param("category") String category);

    @Query("SELECT t FROM TechnologyStack t WHERE t.deleteFlag = false AND t.embedding IS NOT NULL")
    List<TechnologyStack> findAllWithEmbeddings();

    @Query("SELECT t FROM TechnologyStack t WHERE t.deleteFlag = false ORDER BY t.popularityScore DESC")
    List<TechnologyStack> findAllOrderByPopularity();
}
