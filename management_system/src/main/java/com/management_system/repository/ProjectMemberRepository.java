package com.management_system.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.management_system.entity.ProjectMember;

public interface ProjectMemberRepository extends BaseRepository<ProjectMember, UUID> {
    List<ProjectMember> findAllByProjectIdAndDeleteFlagFalse(UUID projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
