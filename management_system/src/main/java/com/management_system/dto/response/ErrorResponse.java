package com.management_system.dto.response;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private List<String> errors;

    public ErrorResponse() {

    }

    public ErrorResponse(List<String> errors) {
        this.errors = errors;
    }

    public ErrorResponse(String msgErrors) {
        this.errors = Arrays.asList(msgErrors.split(","));
    }
}
