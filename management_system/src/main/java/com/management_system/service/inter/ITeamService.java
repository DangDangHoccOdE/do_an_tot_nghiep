package com.management_system.service.inter;

import java.util.UUID;

import com.management_system.dto.request.TeamMemberRequest;
import com.management_system.dto.request.TeamRequest;
import com.management_system.dto.response.TeamResponse;
import com.management_system.dto.response.PageResponse;

public interface ITeamService {
    PageResponse<TeamResponse> getPage(int page, int size);

    TeamResponse get(UUID id);

    TeamResponse create(TeamRequest request);

    TeamResponse update(UUID id, TeamRequest request);

    TeamResponse addMember(UUID teamId, TeamMemberRequest request);

    void removeMember(UUID teamId, UUID userId);
}
