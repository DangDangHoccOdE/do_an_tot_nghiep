package com.management_system.service;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserSecurityService extends UserDetailsService {
    User findByUserName(String username);

    User findByEmail(String email);

}
