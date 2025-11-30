package com.management_system.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DuplicateInfo {
    private String fieldName;
    private UUID duplicateId;
}

