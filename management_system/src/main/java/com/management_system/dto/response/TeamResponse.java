package com.management_system.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID projectId;
    private String projectName;
    private List<TeamMemberResponse> members;
}
