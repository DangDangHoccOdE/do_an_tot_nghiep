package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import com.management_system.entity.Skill;

public interface SkillRepository extends BaseRepository<Skill, UUID> {
    Optional<Skill> findByName(String name);

    Optional<Skill> findByNameAndDeleteFlagFalse(String name);
}
