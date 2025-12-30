package com.management_system.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.management_system.entity.Team;

public interface TeamRepository extends BaseRepository<Team, UUID> {
	Page<Team> findAllByDeleteFlagFalse(Pageable pageable);
}
