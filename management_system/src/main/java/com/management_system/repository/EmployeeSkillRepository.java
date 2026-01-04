package com.management_system.repository;

import java.util.List;
import java.util.UUID;

import com.management_system.entity.EmployeeSkill;

public interface EmployeeSkillRepository extends BaseRepository<EmployeeSkill, UUID> {
    List<EmployeeSkill> findAllByUserIdAndDeleteFlagFalse(UUID userId);

    List<EmployeeSkill> findAllBySkillIdAndDeleteFlagFalse(UUID skillId);

    void deleteByUserIdAndSkillId(UUID userId, UUID skillId);
}
