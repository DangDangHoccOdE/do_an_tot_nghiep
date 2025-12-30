package com.management_system.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamMemberResponse {
    private UUID userId;
    private String roleInTeam;
}
