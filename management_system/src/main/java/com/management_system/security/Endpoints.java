package com.management_system.security;

public class Endpoints {
    public static final String front_end_host = "http://localhost:5173";

    public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/users/register",
            "/api/v1/users/check-duplicate",
            "/users/login",
            "login/oauth2/code/**",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/projects",
    };

    public static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/projects",
    };
}
