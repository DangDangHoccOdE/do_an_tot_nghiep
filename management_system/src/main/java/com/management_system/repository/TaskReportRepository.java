package com.management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.management_system.entity.TaskReport;
import com.management_system.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskReportRepository extends JpaRepository<TaskReport, UUID> {
    @Query("SELECT tr FROM TaskReport tr WHERE tr.dailyTaskId = :dailyTaskId AND tr.deleteFlag = false ORDER BY tr.reportedAt DESC")
    List<TaskReport> findByDailyTaskId(@Param("dailyTaskId") UUID dailyTaskId);

    @Query("SELECT tr FROM TaskReport tr WHERE tr.reportedBy = :reportedBy AND tr.deleteFlag = false ORDER BY tr.reportedAt DESC")
    List<TaskReport> findByReportedBy(@Param("reportedBy") UUID reportedBy);

    @Query("SELECT tr FROM TaskReport tr WHERE tr.reportedBy = :reportedBy AND tr.reportedAt >= :startDate AND tr.reportedAt <= :endDate AND tr.deleteFlag = false")
    List<TaskReport> findByReportedByAndDateRange(@Param("reportedBy") UUID reportedBy,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT tr FROM TaskReport tr WHERE tr.status = :status AND tr.deleteFlag = false ORDER BY tr.reportedAt DESC")
    List<TaskReport> findByStatus(@Param("status") ReportStatus status);

    @Query("SELECT tr FROM TaskReport tr WHERE tr.id = :id AND tr.deleteFlag = false")
    Optional<TaskReport> findByIdNotDeleted(@Param("id") UUID id);

    @Query("SELECT tr FROM TaskReport tr WHERE tr.dailyTaskId = :dailyTaskId AND tr.reportedBy = :reportedBy AND tr.deleteFlag = false")
    List<TaskReport> findByDailyTaskAndReporter(@Param("dailyTaskId") UUID dailyTaskId,
            @Param("reportedBy") UUID reportedBy);

    @Query("SELECT COUNT(tr) FROM TaskReport tr WHERE tr.dailyTaskId = :dailyTaskId AND tr.deleteFlag = false")
    int countReportsByTask(@Param("dailyTaskId") UUID dailyTaskId);
}
