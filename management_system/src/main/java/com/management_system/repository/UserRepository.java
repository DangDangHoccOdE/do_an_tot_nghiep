package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;

public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmailAndDeleteFlagFalse(String email);

    Boolean existsByEmailIgnoreCaseAndDeleteFlagFalse(String email);

    Optional<User> findByProviderAndProviderIdAndDeleteFlagFalse(
            AuthProvider provider,
            String providerId);
}
