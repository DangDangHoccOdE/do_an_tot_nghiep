package com.management_system.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.management_system.entity.TeamMember;

public interface TeamMemberRepository extends BaseRepository<TeamMember, UUID> {
	Optional<TeamMember> findByTeamIdAndUserIdAndDeleteFlagFalse(UUID teamId, UUID userId);

	List<TeamMember> findAllByTeamIdAndDeleteFlagFalse(UUID teamId);

	List<TeamMember> findAllByUserIdAndDeleteFlagFalse(UUID userId);
}
