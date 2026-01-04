package com.management_system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.management_system.entity.User;
import com.management_system.repository.RoleRepository;
import com.management_system.repository.UserRepository;
import com.management_system.service.inter.IUserSecurityService;

@Service
public class UserSecurityServiceImpl implements IUserSecurityService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserSecurityServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByEmail(username);

        String role = roleRepository.findByIdAndDeleteFlagFalse(user.getRoleId())
                .map(r -> r.getName())
                .orElse("ROLE_USER");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(role)));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailAndDeleteFlagFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

}
