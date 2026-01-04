package com.management_system.service.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.management_system.entity.Role;
import com.management_system.repository.RoleRepository;
import com.management_system.service.inter.IRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAllByDeleteFlagFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
