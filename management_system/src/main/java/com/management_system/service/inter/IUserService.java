package com.management_system.service.inter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.management_system.dto.request.UserRequest;
import com.management_system.dto.response.MessageResponse;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.UserResponse;
import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;
import com.management_system.entity.enums.ITRole;

public interface IUserService {
    MessageResponse registerUser(UserRequest userRequest) throws IOException;

    Boolean existsByEmail(String email);

    User findUserByEmail(String email);

    User saveUser(User user);

    User findByProviderAndProviderId(AuthProvider authProvider, String providerId);

    UserResponse createUser(UserRequest request, MultipartFile avatarFile);

    UserResponse updateUser(UUID id, UserRequest request, MultipartFile avatarFile);

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);

    PageResponse<UserResponse> getPage(int page, int size);

    PageResponse<UserResponse> getPageByRole(String roleName, int page, int size);

    PageResponse<UserResponse> getStaffPage(int page, int size, ITRole itRole, String keyword);

    // Customer management (ROLE_USER)
    UserResponse createCustomer(UserRequest request, MultipartFile avatarFile);

    UserResponse updateCustomer(UUID id, UserRequest request, MultipartFile avatarFile);

    // Staff management (ROLE_STAFF)
    UserResponse createStaff(UserRequest request, MultipartFile avatarFile);

    UserResponse updateStaff(UUID id, UserRequest request, MultipartFile avatarFile);

    MessageResponse activateAccount(String email, String activationCode);

    MessageResponse resendActivation(String email);
}
