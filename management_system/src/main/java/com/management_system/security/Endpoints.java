package com.management_system.security;

public class Endpoints {
        public static final String front_end_host = "http://localhost:5173";

        // API Endpoints
        public static final String DAILY_TASKS = "/api/v1/daily-tasks";
        public static final String TASK_REPORTS = "/api/v1/task-reports";
        public static final String PROJECT_METRICS = "/api/v1/projects/metrics";

        public static final String[] PUBLIC_GET_ENDPOINTS = {
                        "/api/v1/roles/get-all",
                        "/api/v1/users/check-duplicate",
                        "/users/login",
                        "login/oauth2/code/**",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/api/v1/skills",
        };

        public static final String[] PUBLIC_POST_ENDPOINTS = {
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/activate",
                        "/api/v1/auth/resend-activation",
                        "/api/v1/users/register",
                        "/oauth2/**",
                        "/login/oauth2/**",
        };
}
