package com.management_system.service.inter;

import java.util.List;
import java.util.UUID;

import com.management_system.dto.request.SkillRequest;
import com.management_system.dto.response.SkillResponse;

public interface ISkillService {
    SkillResponse create(SkillRequest request);

    SkillResponse update(UUID id, SkillRequest request);

    SkillResponse getById(UUID id);

    List<SkillResponse> getAll();

    void delete(UUID id);
}
