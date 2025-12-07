package com.management_system.core;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ValidatorWrapper {
    private final Validator validator;

    /**
     * Validates all constraints on {@code object} using {@see Validator.validate}
     * If this reulst has any vaiolations, throw {@see ConstraintViolationException}
     */
    public <T> void validate(T object, Class<?>... groups) {
        var vaiolations = validator.validate(object, groups);
        if (!vaiolations.isEmpty()) {
            throw new ConstraintViolationException(vaiolations);
        }
    }

    /**
     * Validates all constraints on {@code object} using {@see Validator.validate}
     * If this reulst has any vaiolations, throw {@see ConstraintViolationException}
     */
    public <T> void validateList(List<T> objects, Class<?>... groups) {
        var result = new HashSet<ConstraintViolation<T>>();
        for (var obj : objects) {
            var vaiolations = validator.validate(obj, groups);
            result.addAll(vaiolations);
        }
        if (!result.isEmpty()) {
            throw new ConstraintViolationException(result);
        }
    }

}
