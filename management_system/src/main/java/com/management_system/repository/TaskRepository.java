package com.management_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.management_system.entity.Task;

public interface TaskRepository extends BaseRepository<Task, UUID> {
	List<Task> findAllByProjectIdAndDeleteFlagFalse(UUID projectId, Sort sort);
}
