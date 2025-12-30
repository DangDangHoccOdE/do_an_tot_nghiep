package com.management_system.oauth2.user;

import java.util.Map;

@SuppressWarnings("unchecked")
public class FacebookOAuth2UserInfo extends OAuth2UserInfo {

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getGivenName() {
        String name = (String) attributes.get("name");
        return name != null ? name : "";
    }

    @Override
    public String getFamilyName() {
        return "";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        if (attributes.containsKey("picture")) {
            Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
            Map<String, Object> data = (Map<String, Object>) picture.get("data");
            return (String) data.get("url");
        }
        return null;
    }
}
