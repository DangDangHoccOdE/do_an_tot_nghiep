package com.management_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.request.LoginRequest;
import com.management_system.dto.request.TokenRefreshRequest;
import com.management_system.dto.response.ApiResponse;
import com.management_system.dto.response.JwtResponse;
import com.management_system.entity.Role;
import com.management_system.entity.User;
import com.management_system.repository.RoleRepository;
import com.management_system.service.impl.UserSecurityService;
import com.management_system.utils.JwtUtil;
import org.springframework.security.core.AuthenticationException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserSecurityService userSecurityService;
    private final RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateToken(loginRequest.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());

            User user = userSecurityService.findByEmail(loginRequest.getUsername());
            String role = roleRepository.findByIdAndDeleteFlagFalse(user.getRoleId())
                    .map(Role::getName)
                    .orElse(null);

            String fullName = ((user.getLastName() != null ? user.getLastName() + " " : "")
                    + (user.getFirstName() != null ? user.getFirstName() : "")).trim();

            JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken, user.getId(), user.getEmail(),
                    fullName, role);

            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Tài khoản hoặc mật khẩu không đúng"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader,
            @RequestBody(required = false) TokenRefreshRequest body) {

        String refreshToken = refreshTokenHeader;
        if ((refreshToken == null || refreshToken.isBlank()) && body != null) {
            refreshToken = body.getRefreshToken();
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        boolean valid = jwtUtil.validateRefreshToken(refreshToken, JwtUtil.SECRET_REFRESH_TOKEN);
        if (!valid) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtUtil.extractUserName(refreshToken, JwtUtil.SECRET_REFRESH_TOKEN);
        String newAccess = jwtUtil.generateToken(username);
        String newRefresh = jwtUtil.generateRefreshToken(username);

        User user = userSecurityService.findByEmail(username);
        String role = roleRepository.findByIdAndDeleteFlagFalse(user.getRoleId())
                .map(Role::getName)
                .orElse(null);

        String fullName = ((user.getLastName() != null ? user.getLastName() + " " : "")
                + (user.getFirstName() != null ? user.getFirstName() : "")).trim();

        JwtResponse response = new JwtResponse(newAccess, newRefresh, user.getId(), user.getEmail(), fullName, role);
        return ResponseEntity.ok(response);
    }
}
