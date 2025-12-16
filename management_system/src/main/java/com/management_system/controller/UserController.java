package com.management_system.controller;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.dto.request.UserRequest;
import com.management_system.dto.response.MessageResponse;
import com.management_system.service.inter.IUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService iUserService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody UserRequest userRequest) throws IOException {
        MessageResponse response = iUserService.registerUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> existsByDiscussionName(
            @RequestParam String email) {
        Boolean result = iUserService.existsByEmail(email);
        return ResponseEntity.ok(result);
    }

}
