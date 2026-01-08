package com.management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.management_system.entity.ProjectDailyMetrics;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectDailyMetricsRepository extends JpaRepository<ProjectDailyMetrics, UUID> {
    @Query("SELECT pdm FROM ProjectDailyMetrics pdm WHERE pdm.projectId = :projectId AND pdm.deleteFlag = false ORDER BY pdm.reportDate DESC")
    List<ProjectDailyMetrics> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT pdm FROM ProjectDailyMetrics pdm WHERE pdm.projectId = :projectId AND pdm.reportDate = :reportDate AND pdm.deleteFlag = false")
    Optional<ProjectDailyMetrics> findByProjectIdAndDate(@Param("projectId") UUID projectId,
            @Param("reportDate") LocalDate reportDate);

    @Query("SELECT pdm FROM ProjectDailyMetrics pdm WHERE pdm.projectId = :projectId AND pdm.reportDate >= :startDate AND pdm.reportDate <= :endDate AND pdm.deleteFlag = false ORDER BY pdm.reportDate DESC")
    List<ProjectDailyMetrics> findByProjectIdAndDateRange(@Param("projectId") UUID projectId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT pdm FROM ProjectDailyMetrics pdm WHERE pdm.id = :id AND pdm.deleteFlag = false")
    Optional<ProjectDailyMetrics> findByIdNotDeleted(@Param("id") UUID id);

    @Query("SELECT pdm FROM ProjectDailyMetrics pdm WHERE pdm.deleteFlag = false AND pdm.reportDate = :reportDate ORDER BY pdm.completionRate DESC LIMIT :limit")
    List<ProjectDailyMetrics> findTopProjectsByCompletionRate(@Param("reportDate") LocalDate reportDate,
            @Param("limit") int limit);

    @Query("SELECT pdm FROM ProjectDailyMetrics pdm WHERE pdm.deleteFlag = false AND pdm.reportDate >= :startDate AND pdm.reportDate <= :endDate")
    List<ProjectDailyMetrics> findByDateRange(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
