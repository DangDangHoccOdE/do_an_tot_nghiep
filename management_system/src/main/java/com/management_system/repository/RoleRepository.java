package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import com.management_system.entity.Role;

public interface RoleRepository extends BaseRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
