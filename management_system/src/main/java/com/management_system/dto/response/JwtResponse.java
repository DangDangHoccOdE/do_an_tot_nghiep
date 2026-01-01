package com.management_system.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JwtResponse {
    private String accessTokenJwt;
    private String refreshTokenJwt;
    private UUID userId;
    private String email;
    private String fullName;
    private String role;
}
