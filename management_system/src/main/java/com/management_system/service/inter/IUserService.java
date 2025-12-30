package com.management_system.service.inter;

import java.io.IOException;

import com.management_system.dto.request.UserRequest;
import com.management_system.dto.response.MessageResponse;
import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;

public interface IUserService {
    MessageResponse registerUser(UserRequest userRequest) throws IOException;

    Boolean existsByEmail(String email);

    User findUserByEmail(String email);

    User saveUser(User user);

    User findByProviderAndProviderId(AuthProvider authProvider, String providerId);
}
