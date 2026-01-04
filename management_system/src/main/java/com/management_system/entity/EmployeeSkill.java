package com.management_system.entity;

import java.util.UUID;

import com.management_system.entity.enums.SkillLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_skills", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "skill_id" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkill extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Column(name = "level", nullable = false)
    @Enumerated(EnumType.STRING)
    private SkillLevel level;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience; // số năm kinh nghiệm với skill này
}
