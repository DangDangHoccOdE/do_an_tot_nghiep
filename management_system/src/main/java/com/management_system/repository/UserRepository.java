package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import com.management_system.entity.User;

public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmailAndDeleteFlagFalse(String email);
}
