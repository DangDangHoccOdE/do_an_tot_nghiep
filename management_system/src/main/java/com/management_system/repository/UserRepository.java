package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;

public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmailAndDeleteFlagFalse(String email);

    Boolean existsByEmailIgnoreCaseAndDeleteFlagFalse(String email);

    Optional<User> findByProviderAndProviderIdAndDeleteFlagFalse(
            AuthProvider provider,
            String providerId);

    Page<User> findAllByDeleteFlagFalse(Pageable pageable);

    Page<User> findAllByRoleIdAndDeleteFlagFalse(UUID roleId, Pageable pageable);
}
