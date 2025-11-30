package com.management_system.service.inter;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.management_system.entity.User;

public interface IUserSecurityService extends UserDetailsService {
    User findByEmail(String email);
}
