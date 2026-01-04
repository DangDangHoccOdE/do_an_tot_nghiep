package com.management_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management_system.entity.Role;
import com.management_system.service.inter.IRoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final IRoleService iRoleService;

    @GetMapping("/get-all")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = iRoleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

}
