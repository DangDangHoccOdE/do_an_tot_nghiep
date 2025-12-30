package com.management_system.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class JwtResponse {
    private final String accessTokenJwt;
    private final String refreshTokenJwt;
    private final UUID userId;
    private final String email;
    private final String fullName;
    private final String role;
}
