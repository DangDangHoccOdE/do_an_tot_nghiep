package com.management_system.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.management_system.entity.enums.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillResponse {
    private UUID id;
    private UUID skillId;
    private String skillName; // Tên skill (Java, Python, etc.)
    private SkillLevel level; // JUNIOR, MIDDLE, SENIOR
    private Integer yearsOfExperience;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
