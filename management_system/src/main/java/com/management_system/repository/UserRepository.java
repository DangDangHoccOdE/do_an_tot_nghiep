package com.management_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;
import com.management_system.entity.enums.ITRole;

public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmailAndDeleteFlagFalse(String email);

    Boolean existsByEmailIgnoreCaseAndDeleteFlagFalse(String email);

    Optional<User> findByProviderAndProviderIdAndDeleteFlagFalse(
            AuthProvider provider,
            String providerId);

    Page<User> findAllByDeleteFlagFalse(Pageable pageable);

    Page<User> findAllByRoleIdAndDeleteFlagFalse(UUID roleId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleteFlag = false AND u.roleId = :roleId "
            + "AND (:itRole IS NULL OR u.itRole = :itRole) "
            + "AND (:pattern IS NULL OR LOWER(CONCAT(COALESCE(u.firstName,''), ' ', COALESCE(u.lastName,''), ' ', COALESCE(u.email,''))) LIKE :pattern)")
    Page<User> searchStaff(@Param("roleId") UUID roleId,
            @Param("itRole") ITRole itRole,
            @Param("pattern") String pattern,
            Pageable pageable);
}
