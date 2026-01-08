package com.management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.management_system.entity.DailyTask;
import com.management_system.enums.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyTaskRepository extends JpaRepository<DailyTask, UUID> {
    @Query("SELECT dt FROM DailyTask dt WHERE dt.projectId = :projectId AND dt.deleteFlag = false ORDER BY dt.taskDate DESC, dt.priority DESC")
    List<DailyTask> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT dt FROM DailyTask dt WHERE dt.projectId = :projectId AND dt.taskDate = :taskDate AND dt.deleteFlag = false ORDER BY dt.priority DESC")
    List<DailyTask> findByProjectIdAndDate(@Param("projectId") UUID projectId, @Param("taskDate") LocalDate taskDate);

    @Query("SELECT dt FROM DailyTask dt WHERE dt.assignedTo = :assignedTo AND dt.deleteFlag = false ORDER BY dt.taskDate DESC")
    List<DailyTask> findByAssignedTo(@Param("assignedTo") UUID assignedTo);

    @Query("SELECT dt FROM DailyTask dt WHERE dt.assignedTo = :assignedTo AND dt.taskDate = :taskDate AND dt.deleteFlag = false")
    List<DailyTask> findByAssignedToAndDate(@Param("assignedTo") UUID assignedTo,
            @Param("taskDate") LocalDate taskDate);

    @Query("SELECT dt FROM DailyTask dt WHERE dt.projectId = :projectId AND dt.taskDate >= :startDate AND dt.taskDate <= :endDate AND dt.deleteFlag = false")
    List<DailyTask> findByProjectIdAndDateRange(@Param("projectId") UUID projectId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT dt FROM DailyTask dt WHERE dt.status = :status AND dt.projectId = :projectId AND dt.deleteFlag = false")
    List<DailyTask> findByStatusAndProject(@Param("status") TaskStatus status,
            @Param("projectId") UUID projectId);

    @Query("SELECT dt FROM DailyTask dt WHERE dt.id = :id AND dt.deleteFlag = false")
    Optional<DailyTask> findByIdNotDeleted(@Param("id") UUID id);

    @Query("SELECT COUNT(dt) FROM DailyTask dt WHERE dt.projectId = :projectId AND dt.taskDate = :taskDate AND dt.deleteFlag = false")
    int countTasksByProjectAndDate(@Param("projectId") UUID projectId, @Param("taskDate") LocalDate taskDate);

    @Query("SELECT COUNT(dt) FROM DailyTask dt WHERE dt.projectId = :projectId AND dt.taskDate = :taskDate AND dt.status = :status AND dt.deleteFlag = false")
    int countTasksByStatusAndDate(@Param("projectId") UUID projectId, @Param("taskDate") LocalDate taskDate,
            @Param("status") TaskStatus status);
}
