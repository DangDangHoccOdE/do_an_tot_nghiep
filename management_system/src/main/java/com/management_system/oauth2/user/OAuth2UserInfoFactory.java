package com.management_system.oauth2.user;

import java.util.Map;

import com.management_system.entity.enums.AuthProvider;
import com.management_system.exception.OAuth2AuthenticationProcessingException;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.facebook.name())) {
            return new FacebookOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Login with " + registrationId + " is not supported");
        }
    }
}
