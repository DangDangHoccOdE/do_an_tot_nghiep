package com.management_system.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.management_system.constant.ErrorCode;
import com.management_system.constant.MessageKey;
import com.management_system.core.ValidatorWrapper;
import com.management_system.dto.request.UserRequest;
import com.management_system.dto.response.MessageResponse;
import com.management_system.dto.response.PageResponse;
import com.management_system.dto.response.UserResponse;
import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;
import com.management_system.entity.enums.Gender;
import com.management_system.entity.enums.ITRole;
import com.management_system.exception.ResourceAlreadyExistsException;
import com.management_system.repository.RoleRepository;
import com.management_system.repository.UserRepository;
import com.management_system.dto.request.EmployeeSkillRequest;
import com.management_system.service.inter.ICloudinaryService;
import com.management_system.service.inter.IEmployeeSkillService;
import com.management_system.service.inter.IUserService;
import com.management_system.utils.EmailUtil;
import com.management_system.utils.MessageUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final MessageUtil messageUtil;
    private final ValidatorWrapper validatorWrapper;
    private final EmailUtil emailUtil;
    private final ICloudinaryService cloudinaryService;
    private final IEmployeeSkillService employeeSkillService;

    @Value("${file.default-avatar}")
    private String defaultAvatar;

    @Value("${default-role-id}")
    private String defaultRole;

    @Value("${activation.expiry-minutes:10}")
    private long activationExpiryMinutes;

    @Override
    @Transactional
    public MessageResponse registerUser(UserRequest userRequest) throws IOException {
        validatorWrapper.validate(userRequest);

        User user = modelMapper.map(userRequest, User.class);

        if (userRepository.existsByEmailIgnoreCaseAndDeleteFlagFalse(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    messageUtil.getMessage(
                            ErrorCode.ERR002,
                            new Object[] { MessageKey.EMAIL },
                            LocaleContextHolder.getLocale()));
        }

        user.setAvatar(resolveAvatarForCreate(null, userRequest.getAvatar()));

        // handle password
        String encryptPassword = bCryptPasswordEncoder.encode(userRequest.getPassword());
        user.setPassword(encryptPassword);

        setAuthProvider(user, userRequest.getAuthProvider());
        setRole(user, userRequest.getRoleId());

        assignActivationCode(user);

        // Save user to database
        user = userRepository.save(user);

        // Send activation email
        emailUtil.sendEmailActive(userRequest.getEmail(), user.getActivationCode());

        String msg = messageUtil.getMessage(MessageKey.REGISTER_SUCCESS, null, LocaleContextHolder.getLocale());
        return new MessageResponse(msg);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCaseAndDeleteFlagFalse(email);
    }

    @Override
    @Transactional
    public MessageResponse activateAccount(String email, String activationCode) {
        User user = userRepository.findByEmailAndDeleteFlagFalse(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.format(ErrorCode.ERR003, MessageKey.USER, LocaleContextHolder.getLocale())));

        if (Boolean.TRUE.equals(user.getActive())) {
            String msg = messageUtil.getMessage("activation.already", null, LocaleContextHolder.getLocale());
            return new MessageResponse(msg);
        }

        if (user.getActivationCode() == null || !user.getActivationCode().equals(activationCode)) {
            String msg = messageUtil.getMessage("activation.invalid", null, LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        if (user.getActivationExpiry() != null && user.getActivationExpiry().isBefore(LocalDateTime.now())) {
            assignActivationCode(user);
            userRepository.save(user);
            emailUtil.sendEmailActive(user.getEmail(), user.getActivationCode());
            String msg = messageUtil.getMessage("activation.expired", null, LocaleContextHolder.getLocale());
            throw new IllegalStateException(msg);
        }

        user.setActive(true);
        user.setActivationCode(null);
        user.setActivationExpiry(null);
        userRepository.save(user);

        String msg = messageUtil.getMessage("activation.success", null, LocaleContextHolder.getLocale());
        return new MessageResponse(msg);
    }

    @Override
    @Transactional
    public MessageResponse resendActivation(String email) {
        User user = userRepository.findByEmailAndDeleteFlagFalse(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.format(ErrorCode.ERR003, MessageKey.USER, LocaleContextHolder.getLocale())));

        if (Boolean.TRUE.equals(user.getActive())) {
            String msg = messageUtil.getMessage("activation.already", null, LocaleContextHolder.getLocale());
            return new MessageResponse(msg);
        }

        assignActivationCode(user);
        userRepository.save(user);
        emailUtil.sendEmailActive(user.getEmail(), user.getActivationCode());

        String msg = messageUtil.getMessage("activation.resent", null, LocaleContextHolder.getLocale());
        return new MessageResponse(msg);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailAndDeleteFlagFalse(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageUtil.format(ErrorCode.ERR003, MessageKey.USER, LocaleContextHolder.getLocale())));
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByProviderAndProviderId(AuthProvider authProvider, String providerId) {
        return userRepository.findByProviderAndProviderIdAndDeleteFlagFalse(authProvider, providerId)
                .orElse(null);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request, MultipartFile avatarFile) {
        validatorWrapper.validate(request);
        ensureEmailNotExists(request.getEmail());

        User user = new User();
        applyRequest(user, request);
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setAvatar(resolveAvatarForCreate(avatarFile, request.getAvatar()));
        setAuthProvider(user, request.getAuthProvider());
        setRole(user, request.getRoleId());

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request, MultipartFile avatarFile) {
        validatorWrapper.validate(request);

        User user = userRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        applyRequest(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleId() != null) {
            user.setRoleId(request.getRoleId());
        }

        handleAvatarUpdate(user, avatarFile, request.getAvatar());
        setAuthProvider(user, request.getAuthProvider());

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Delete associated employee skills first (cascade soft delete)
        employeeSkillService.deleteEmployeeSkills(id);

        deleteAvatarIfNeeded(user.getAvatar());
        user.setDeleteFlag(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUsers(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (UUID id : ids) {
            deleteUser(id);
        }
    }

    @Override
    public PageResponse<UserResponse> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> pageResult = userRepository.findAllByDeleteFlagFalse(pageable);

        return new PageResponse<>(
                pageResult.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                page,
                size);
    }

    @Override
    public PageResponse<UserResponse> getPageByRole(String roleName, int page, int size) {
        // Find role ID by name
        UUID roleId = roleRepository.findByName(roleName)
                .map(role -> role.getId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        Pageable pageable = PageRequest.of(page, size);
        Page<User> pageResult = userRepository.findAllByRoleIdAndDeleteFlagFalse(roleId, pageable);

        return new PageResponse<>(
                pageResult.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                page,
                size);
    }

    @Override
    public PageResponse<UserResponse> getStaffPage(int page, int size, ITRole itRole, String keyword) {
        UUID roleId = roleRepository.findByName("ROLE_STAFF")
                .map(role -> role.getId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: ROLE_STAFF"));

        Pageable pageable = PageRequest.of(page, size);
        String pattern = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.trim().toLowerCase() + "%"
                : null;

        Page<User> pageResult = userRepository.searchStaff(roleId, itRole, pattern, pageable);

        return new PageResponse<>(
                pageResult.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                page,
                size);
    }

    private void applyRequest(User user, UserRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()) {
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }

        if (request.getGender() != null) {
            try {
                user.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                user.setGender(null);
            }
        }

        if (request.getSkill() != null) {
            user.setSkill(request.getSkill());
        }

        if (request.getCv() != null) {
            user.setCv(request.getCv());
        }

        if (request.getItRole() != null) {
            user.setItRole(request.getItRole());
        }
    }

    private void ensureEmailNotExists(String email) {
        if (userRepository.existsByEmailIgnoreCaseAndDeleteFlagFalse(email)) {
            throw new ResourceAlreadyExistsException(
                    messageUtil.getMessage(
                            ErrorCode.ERR002,
                            new Object[] { MessageKey.EMAIL },
                            LocaleContextHolder.getLocale()));
        }
    }

    private void setAuthProvider(User user, String authProvider) {
        if (authProvider != null) {
            try {
                user.setProvider(AuthProvider.valueOf(authProvider.toLowerCase()));
                return;
            } catch (IllegalArgumentException e) {
                // Fallback below
            }
        }
        user.setProvider(AuthProvider.local);
    }

    private void setRole(User user, UUID roleId) {
        if (roleId != null) {
            user.setRoleId(roleId);
            return;
        }
        UUID defaultRoleId = UUID.fromString(defaultRole);
        user.setRoleId(defaultRoleId);
    }

    private void assignActivationCode(User user) {
        user.setActive(false);
        user.setActivationCode(UUID.randomUUID().toString());
        user.setActivationExpiry(LocalDateTime.now().plusMinutes(activationExpiryMinutes));
    }

    private String resolveAvatarForCreate(MultipartFile avatarFile, String avatarUrlFromRequest) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            return cloudinaryService.uploadImage(avatarFile);
        }
        if (avatarUrlFromRequest != null && !avatarUrlFromRequest.isBlank()) {
            return avatarUrlFromRequest;
        }
        return defaultAvatar;
    }

    private void handleAvatarUpdate(User user, MultipartFile avatarFile, String avatarUrlFromRequest) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String newAvatarUrl = cloudinaryService.uploadImage(avatarFile);
            deleteAvatarIfNeeded(user.getAvatar());
            user.setAvatar(newAvatarUrl);
            return;
        }

        if (avatarUrlFromRequest != null && !avatarUrlFromRequest.isBlank()) {
            user.setAvatar(avatarUrlFromRequest);
        }
    }

    private void deleteAvatarIfNeeded(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }
        if (defaultAvatar != null && avatarUrl.equals(defaultAvatar)) {
            return;
        }
        cloudinaryService.deleteImage(avatarUrl);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .roleId(user.getRoleId())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .provider(user.getProvider())
                .skill(user.getSkill())
                .cv(user.getCv())
                .skills(employeeSkillService.getEmployeeSkills(user.getId()))
                .itRole(user.getItRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserResponse createCustomer(UserRequest request, MultipartFile avatarFile) {
        validatorWrapper.validate(request);
        ensureEmailNotExists(request.getEmail());

        User user = new User();
        applyRequest(user, request);
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setAvatar(resolveAvatarForCreate(avatarFile, request.getAvatar()));
        setAuthProvider(user, request.getAuthProvider());

        // Set ROLE_USER for customers
        UUID userRoleId = roleRepository.findByName("ROLE_USER")
                .map(role -> role.getId())
                .orElse(UUID.fromString(defaultRole));
        user.setRoleId(userRoleId);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateCustomer(UUID id, UserRequest request, MultipartFile avatarFile) {
        validatorWrapper.validate(request);

        User user = userRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        applyRequest(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        }

        handleAvatarUpdate(user, avatarFile, request.getAvatar());
        setAuthProvider(user, request.getAuthProvider());
        // Keep ROLE_USER for customers - don't change it

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse createStaff(UserRequest request, MultipartFile avatarFile) {
        validatorWrapper.validate(request);
        ensureEmailNotExists(request.getEmail());

        User user = new User();
        applyRequest(user, request);
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setAvatar(resolveAvatarForCreate(avatarFile, request.getAvatar()));
        setAuthProvider(user, request.getAuthProvider());

        // Set ROLE_STAFF for staff members
        UUID staffRoleId = roleRepository.findByName("ROLE_STAFF")
                .map(role -> role.getId())
                .orElseThrow(() -> new EntityNotFoundException("ROLE_STAFF not found"));
        user.setRoleId(staffRoleId);

        User savedUser = userRepository.save(user);

        // Add skills to staff if provided
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            for (EmployeeSkillRequest skillRequest : request.getSkills()) {
                employeeSkillService.addSkillToEmployee(savedUser.getId(), skillRequest);
            }
        }

        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateStaff(UUID id, UserRequest request, MultipartFile avatarFile) {
        validatorWrapper.validate(request);

        User user = userRepository.findByIdAndDeleteFlagFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        applyRequest(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        }

        handleAvatarUpdate(user, avatarFile, request.getAvatar());
        setAuthProvider(user, request.getAuthProvider());
        // Keep ROLE_STAFF for staff - don't change it

        User updatedUser = userRepository.save(user);

        // Update staff skills
        if (request.getSkills() != null) {
            // Delete existing skills first
            employeeSkillService.deleteEmployeeSkills(id);

            // Add new skills
            if (!request.getSkills().isEmpty()) {
                for (EmployeeSkillRequest skillRequest : request.getSkills()) {
                    employeeSkillService.addSkillToEmployee(id, skillRequest);
                }
            }
        }

        return toResponse(updatedUser);
    }
}
