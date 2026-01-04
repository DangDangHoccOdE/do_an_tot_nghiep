package com.management_system.dto.request;

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
public class EmployeeSkillRequest {
    private UUID skillId;
    private SkillLevel level; // JUNIOR, MIDDLE, SENIOR
    private Integer yearsOfExperience;
}
