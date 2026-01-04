package com.management_system.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.management_system.entity.enums.AuthProvider;
import com.management_system.entity.enums.Gender;
import com.management_system.entity.enums.ITRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID roleId;
    private String avatar;
    private Gender gender;
    private AuthProvider provider;
    private String skill; // For staff: "Middle Java, Senior Python" (deprecated)
    private String cv; // For staff: CV file URL
    private List<EmployeeSkillResponse> skills; // For staff: List of employee skills
    private ITRole itRole; // IT Role: DEV, TEST, BA, PM, etc.
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
