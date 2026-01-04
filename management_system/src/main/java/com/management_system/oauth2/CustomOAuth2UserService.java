package com.management_system.oauth2;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.management_system.entity.Role;
import com.management_system.entity.User;
import com.management_system.entity.enums.AuthProvider;
import com.management_system.exception.OAuth2AuthenticationProcessingException;
import com.management_system.oauth2.user.OAuth2UserInfo;
import com.management_system.oauth2.user.OAuth2UserInfoFactory;
import com.management_system.repository.RoleRepository;
import com.management_system.service.impl.UserServiceImpl;

//Lớp CustomOAuth2UserService chịu trách nhiệm:
//Xử lý thông tin người dùng từ OAuth2 provider.
//Tìm kiếm hoặc đăng ký người dùng mới trong hệ thống.
//Cập nhật thông tin người dùng đã tồn tại.
//Đảm bảo rằng người dùng đang đăng nhập bằng cùng một provider mà họ đã sử dụng để đăng ký.
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserServiceImpl userService;
    private final RoleRepository roleRepository;

    @Value("${default-role-id}")
    private String defaultRole;

    @Autowired
    public CustomOAuth2UserService(@Lazy UserServiceImpl userService, @Lazy RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        if (registrationId.equalsIgnoreCase(AuthProvider.google.name())
                && !StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException(
                    "Email not found from Google provider");
        }

        User userOptional = userService.findByProviderAndProviderId(
                AuthProvider.valueOf(registrationId),
                oAuth2UserInfo.getId());
        User user;
        if (userOptional != null) {
            user = userOptional;
            // if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId())))
            // {
            // throw new OAuth2AuthenticationProcessingException("Có vẻ như bạn đã đăng ký
            // với " +
            // user.getProvider() + " tài khoản. Vui lòng sử dụng " + user.getProvider() +
            // " tài khoản để đăng nhập.");
            // } // Nếu sau này không muốn ng dùng k cần đăng ký mà chỉ cần dùng tài khoản
            // gg vừa login và signup
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        return UserPrincipal.create(user, oAuth2User.getAttributes(), role.getName());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setFirstName(oAuth2UserInfo.getGivenName());
        user.setLastName(oAuth2UserInfo.getFamilyName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setAvatar(oAuth2UserInfo.getImageUrl());

        UUID defaultRoleId = UUID.fromString(defaultRole);
        user.setRoleId(defaultRoleId);
        return userService.saveUser(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setFirstName(oAuth2UserInfo.getGivenName());
        existingUser.setLastName(oAuth2UserInfo.getFamilyName());
        return userService.saveUser(existingUser);
    }
}