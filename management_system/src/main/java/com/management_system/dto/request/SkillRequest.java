package com.management_system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {
    private String name; // Java, Python, PHP, etc.
    private String description;
    private String category; // Backend, Frontend, Mobile, DevOps, etc.
}
