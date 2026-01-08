package com.management_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.management_system.entity.Project;

public interface ProjectRepository extends BaseRepository<Project, UUID> {
	Page<Project> findAllByDeleteFlagFalse(Pageable pageable);

	List<Project> findAllByTeamIdInAndDeleteFlagFalse(List<UUID> teamIds);

	boolean existsByProjectNameAndDeleteFlagFalse(String projectName);

	boolean existsByProjectNameAndIdNotAndDeleteFlagFalse(String projectName, UUID id);

	List<Project> findAllByDeleteFlagFalse();
}
