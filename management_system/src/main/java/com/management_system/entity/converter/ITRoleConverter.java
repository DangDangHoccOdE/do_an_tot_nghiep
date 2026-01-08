package com.management_system.entity.converter;

import com.management_system.entity.enums.ITRole;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ITRoleConverter implements AttributeConverter<ITRole, String> {

    @Override
    public String convertToDatabaseColumn(ITRole attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public ITRole convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return ITRole.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Unknown value stored in DB, fallback to null to avoid runtime failures
            return null;
        }
    }
}
