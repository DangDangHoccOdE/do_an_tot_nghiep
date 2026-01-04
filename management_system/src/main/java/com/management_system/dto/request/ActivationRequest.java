package com.management_system.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivationRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String activationCode;
}
