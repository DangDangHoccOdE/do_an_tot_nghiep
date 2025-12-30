package com.management_system.dto.request;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamMemberRequest {
    private UUID userId;
    private String roleInTeam;
}
