package com.management_system.service.inter;

import java.io.IOException;
import java.util.Locale;

import com.management_system.dto.request.UserRequest;
import com.management_system.dto.response.MessageResponse;

public interface IUserService {
    MessageResponse registerUser(UserRequest userRequest, Locale locale) throws IOException;

    Boolean existsByEmail(String email);
}
