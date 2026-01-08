package com.management_system.dto.response;

import java.util.UUID;

import com.management_system.entity.enums.ITRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private ITRole itRole;
}
