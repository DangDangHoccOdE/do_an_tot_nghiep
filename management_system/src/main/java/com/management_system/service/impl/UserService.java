package com.management_system.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.management_system.constant.ErrorCode;
import com.management_system.constant.MessageKey;
import com.management_system.core.ValidatorWrapper;
import com.management_system.dto.request.UserRequest;
import com.management_system.dto.response.MessageResponse;
import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;
import com.management_system.exception.ResourceAlreadyExistsException;
import com.management_system.repository.UserRepository;
import com.management_system.service.inter.FileStorageService;
import com.management_system.service.inter.IUserService;
import com.management_system.utils.EmailUtil;
import com.management_system.utils.MessageUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final MessageUtil messageUtil;
    private final ValidatorWrapper validatorWrapper;
    private final FileStorageService fileStorageService;
    private final EmailUtil emailUtil;

    @Value("${file.default-avatar}")
    private String defaultAvatar;

    @Value("${default-role-id}")
    private String defaultRole;

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

        // handle avatar
        MultipartFile avatar = userRequest.getAvatar();
        String avatarPath = defaultAvatar;

        if (avatar != null && !avatar.isEmpty()) {
            avatarPath = fileStorageService.storeFile(avatar, LocaleContextHolder.getLocale());
        } else {
            user.setAvatar(avatarPath);
        }

        // handle password
        String encryptPassword = bCryptPasswordEncoder.encode(userRequest.getPassword());
        user.setPassword(encryptPassword);

        // Set auth provider
        if (userRequest.getAuthProvider() != null) {
            try {
                user.setProvider(AuthProvider.valueOf(userRequest.getAuthProvider().toLowerCase()));
            } catch (IllegalArgumentException e) {
                user.setProvider(AuthProvider.local);
            }
        } else {
            user.setProvider(AuthProvider.local);
        }

        UUID defaultRoleId = UUID.fromString(defaultRole);
        user.setRoleId(defaultRoleId);

        // Generate activation code
        String activationCode = UUID.randomUUID().toString();
        user.setActivationCode(activationCode);
        user.setActivationExpiry(LocalDateTime.now().plusMinutes(10));

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

}
