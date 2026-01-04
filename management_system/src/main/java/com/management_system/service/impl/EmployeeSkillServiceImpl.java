package com.management_system.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.management_system.dto.request.EmployeeSkillRequest;
import com.management_system.dto.response.EmployeeSkillResponse;
import com.management_system.entity.EmployeeSkill;
import com.management_system.entity.Skill;
import com.management_system.repository.EmployeeSkillRepository;
import com.management_system.repository.SkillRepository;
import com.management_system.service.inter.IEmployeeSkillService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeSkillServiceImpl implements IEmployeeSkillService {
    private final EmployeeSkillRepository employeeSkillRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public EmployeeSkillResponse addSkillToEmployee(UUID employeeId, EmployeeSkillRequest request) {
        Skill skill = skillRepository.findByIdAndDeleteFlagFalse(request.getSkillId())
                .orElseThrow(() -> new EntityNotFoundException("Skill not found"));

        EmployeeSkill employeeSkill = EmployeeSkill.builder()
                .userId(employeeId)
                .skillId(request.getSkillId())
                .level(request.getLevel())
                .yearsOfExperience(request.getYearsOfExperience())
                .build();

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);
        return toResponse(saved, skill.getName());
    }

    @Override
    @Transactional
    public void removeSkillFromEmployee(UUID employeeId, UUID skillId) {
        employeeSkillRepository.deleteByUserIdAndSkillId(employeeId, skillId);
    }

    @Override
    public List<EmployeeSkillResponse> getEmployeeSkills(UUID employeeId) {
        List<EmployeeSkill> employeeSkills = employeeSkillRepository.findAllByUserIdAndDeleteFlagFalse(employeeId);
        return employeeSkills.stream()
                .map(es -> {
                    // Fetch skill name by ID instead of using relationship
                    String skillName = skillRepository.findById(es.getSkillId())
                            .map(Skill::getName)
                            .orElse("Unknown");
                    return toResponse(es, skillName);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEmployeeSkills(UUID employeeId) {
        List<EmployeeSkill> employeeSkills = employeeSkillRepository.findAllByUserIdAndDeleteFlagFalse(employeeId);
        employeeSkills.forEach(es -> {
            es.setDeleteFlag(true);
            employeeSkillRepository.save(es);
        });
    }

    private EmployeeSkillResponse toResponse(EmployeeSkill employeeSkill, String skillName) {
        return EmployeeSkillResponse.builder()
                .id(employeeSkill.getId())
                .skillId(employeeSkill.getSkillId())
                .skillName(skillName)
                .level(employeeSkill.getLevel())
                .yearsOfExperience(employeeSkill.getYearsOfExperience())
                .createdAt(employeeSkill.getCreatedAt())
                .updatedAt(employeeSkill.getUpdatedAt())
                .build();
    }
}
