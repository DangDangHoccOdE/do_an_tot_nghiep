package com.management_system.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.management_system.dto.request.SkillRequest;
import com.management_system.dto.response.SkillResponse;
import com.management_system.entity.Skill;
import com.management_system.repository.SkillRepository;
import com.management_system.service.inter.ISkillService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements ISkillService {
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public SkillResponse create(SkillRequest request) {
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setCategory(request.getCategory());

        Skill saved = skillRepository.save(skill);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SkillResponse update(UUID id, SkillRequest request) {
        Skill skill = skillRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill not found"));

        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setCategory(request.getCategory());

        Skill updated = skillRepository.save(skill);
        return toResponse(updated);
    }

    @Override
    public SkillResponse getById(UUID id) {
        Skill skill = skillRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill not found"));
        return toResponse(skill);
    }

    @Override
    public List<SkillResponse> getAll() {
        return skillRepository.findAllByDeleteFlagFalse(org.springframework.data.domain.Sort.by("name"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Skill skill = skillRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill not found"));
        skill.setDeleteFlag(true);
        skillRepository.save(skill);
    }

    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .category(skill.getCategory())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}
