package com.management_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.request.LoginRequest;
import com.management_system.dto.request.ActivationRequest;
import com.management_system.dto.request.ResendActivationRequest;
import com.management_system.dto.request.TokenRefreshRequest;
import com.management_system.dto.response.ApiResponse;
import com.management_system.dto.response.JwtResponse;
import com.management_system.dto.response.MessageResponse;
import com.management_system.entity.Role;
import com.management_system.entity.User;
import com.management_system.repository.RoleRepository;
import com.management_system.service.impl.UserSecurityServiceImpl;
import com.management_system.service.inter.IUserService;
import com.management_system.utils.JwtUtil;
import com.management_system.utils.MessageUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.context.i18n.LocaleContextHolder;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserSecurityServiceImpl userSecurityService;
    private final RoleRepository roleRepository;
    private final IUserService userService;
    private final MessageUtil messageUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userSecurityService.findByEmail(loginRequest.getUsername());

            if (!Boolean.TRUE.equals(user.getActive())) {
                String msg = messageUtil.getMessage("ERR403", null, LocaleContextHolder.getLocale());
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, msg));
            }

            String accessToken = jwtUtil.generateToken(loginRequest.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());
            String role = roleRepository.findByIdAndDeleteFlagFalse(user.getRoleId())
                    .map(Role::getName)
                    .orElse(null);

            String fullName = ((user.getLastName() != null ? user.getLastName() + " " : "")
                    + (user.getFirstName() != null ? user.getFirstName() : "")).trim();

            JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken, user.getId(), user.getEmail(),
                    fullName, role);

            String successMsg = messageUtil.getMessage("SUCC004", null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(ApiResponse.success(successMsg, jwtResponse));
        } catch (BadCredentialsException e) {
            String errorMsg = messageUtil.getMessage("ERR401", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(401).body(ApiResponse.error(401, errorMsg));
        } catch (Exception e) {
            String errorMsg = messageUtil.getMessage("ERR500", new Object[] { e.getMessage() },
                    LocaleContextHolder.getLocale());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, errorMsg));
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestBody ActivationRequest request) {
        try {
            MessageResponse response = userService.activateAccount(request.getEmail(), request.getActivationCode());
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(410).body(ApiResponse.error(410, e.getMessage()));
        } catch (Exception e) {
            String errorMsg = messageUtil.getMessage("ERR500", new Object[] { e.getMessage() },
                    LocaleContextHolder.getLocale());
            return ResponseEntity.status(500).body(ApiResponse.error(500, errorMsg));
        }
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<?> resendActivation(@RequestBody ResendActivationRequest request) {
        try {
            MessageResponse response = userService.resendActivation(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            String errorMsg = messageUtil.getMessage("ERR500", new Object[] { e.getMessage() },
                    LocaleContextHolder.getLocale());
            return ResponseEntity.status(500).body(ApiResponse.error(500, errorMsg));
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
