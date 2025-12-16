package com.management_system.security;

public class Endpoints {
    public static final String front_end_host = "http://localhost:5173";

    public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/users/register",
            "/users/login",
            "/oauth2/authorize/google",
            "/oauth2/authorize/facebook"
    };
}
