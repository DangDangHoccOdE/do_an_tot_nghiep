package com.management_system.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.management_system.dto.request.UserRequest;
import com.management_system.dto.request.EmployeeSkillRequest;
import com.management_system.dto.response.MessageResponse;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.UserResponse;
import com.management_system.dto.response.EmployeeSkillResponse;
import com.management_system.entity.enums.ITRole;
import com.management_system.service.inter.IUserService;
import com.management_system.service.inter.IEmployeeSkillService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService iUserService;
    private final IEmployeeSkillService employeeSkillService;
    private final ObjectMapper objectMapper;

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<UserResponse> response = iUserService.getPage(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<UserResponse> response = iUserService.getPageByRole("ROLE_USER", page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> listStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<UserResponse> response = iUserService.getPageByRole("ROLE_STAFF", page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserDetail(@PathVariable UUID id) {
        UserResponse response = iUserService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("roleId") UUID roleId,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        UserRequest request = UserRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roleId(roleId)
                .authProvider("local")
                .build();

        UserResponse response = iUserService.createUser(request, avatar);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestParam("email") String email,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("roleId") UUID roleId,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        UserRequest request = UserRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roleId(roleId)
                .authProvider("local")
                .build();

        UserResponse response = iUserService.updateUser(id, request, avatar);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        iUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // =================== CUSTOMERS MANAGEMENT (ROLE_USER) ===================

    @PostMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createCustomer(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        UserRequest request = UserRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roleId(null) // Will be set to ROLE_USER in service
                .authProvider("local")
                .build();

        UserResponse response = iUserService.createCustomer(request, avatar);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateCustomer(
            @PathVariable UUID id,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        UserRequest request = UserRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .authProvider("local")
                .build();

        UserResponse response = iUserService.updateCustomer(id, request, avatar);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        iUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // =================== STAFF MANAGEMENT (ROLE_STAFF) ===================

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createStaff(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "cv", required = false) String cv,
            @RequestParam(value = "skills", required = false) String skillsJson,
            @RequestParam(value = "itRole", required = false) String itRoleStr,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws IOException {

        // Parse skills JSON if provided
        List<EmployeeSkillRequest> skillsList = null;
        if (skillsJson != null && !skillsJson.isEmpty()) {
            skillsList = objectMapper.readValue(skillsJson, new TypeReference<List<EmployeeSkillRequest>>() {
            });
        }

        // Parse IT Role if provided
        ITRole itRole = null;
        if (itRoleStr != null && !itRoleStr.isEmpty()) {
            try {
                itRole = ITRole.valueOf(itRoleStr);
            } catch (IllegalArgumentException e) {
                // Handle invalid IT Role
            }
        }

        UserRequest request = UserRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .skill(skill)
                .cv(cv)
                .skills(skillsList)
                .itRole(itRole)
                .roleId(null) // Will be set to ROLE_STAFF in service
                .authProvider("local")
                .build();

        UserResponse response = iUserService.createStaff(request, avatar);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateStaff(
            @PathVariable UUID id,
            @RequestParam("email") String email,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "cv", required = false) String cv,
            @RequestParam(value = "skills", required = false) String skillsJson,
            @RequestParam(value = "itRole", required = false) String itRoleStr,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws IOException {

        // Parse skills JSON if provided
        List<EmployeeSkillRequest> skillsList = null;
        if (skillsJson != null && !skillsJson.isEmpty()) {
            skillsList = objectMapper.readValue(skillsJson, new TypeReference<List<EmployeeSkillRequest>>() {
            });
        }

        // Parse IT Role if provided
        ITRole itRole = null;
        if (itRoleStr != null && !itRoleStr.isEmpty()) {
            try {
                itRole = ITRole.valueOf(itRoleStr);
            } catch (IllegalArgumentException e) {
                // Handle invalid IT Role
            }
        }

        UserRequest request = UserRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .skill(skill)
                .cv(cv)
                .skills(skillsList)
                .itRole(itRole)
                .authProvider("local")
                .build();

        UserResponse response = iUserService.updateStaff(id, request, avatar);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStaff(@PathVariable UUID id) {
        iUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Staff Skills Management Endpoints
    @PostMapping("/staff/{staffId}/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeSkillResponse> addSkillToStaff(
            @PathVariable UUID staffId,
            @RequestBody EmployeeSkillRequest request) {
        EmployeeSkillResponse response = employeeSkillService.addSkillToEmployee(staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/staff/{staffId}/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<EmployeeSkillResponse>> getStaffSkills(@PathVariable UUID staffId) {
        java.util.List<EmployeeSkillResponse> response = employeeSkillService.getEmployeeSkills(staffId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/staff/{staffId}/skills/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeSkillFromStaff(
            @PathVariable UUID staffId,
            @PathVariable UUID skillId) {
        employeeSkillService.removeSkillFromEmployee(staffId, skillId);
        return ResponseEntity.noContent().build();
    }

}
