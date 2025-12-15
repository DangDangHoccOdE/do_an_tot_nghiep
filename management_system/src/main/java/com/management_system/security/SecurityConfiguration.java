package com.management_system.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.management_system.filter.JwtFilter;
import com.management_system.oauth2.CustomOAuth2UserService;
import com.management_system.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.management_system.oauth2.OAuth2AuthenticationFailureHandler;
import com.management_system.oauth2.OAuth2AuthenticationSuccessHandler;
import com.management_system.service.impl.UserSecurityService;
import com.management_system.service.inter.IUserSecurityService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    private CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(handlerExceptionResolver);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Autowired
    public DaoAuthenticationProvider authenticationProvider(IUserSecurityService IUserSecurityService) {
        DaoAuthenticationProvider dap = new DaoAuthenticationProvider();
        dap.setUserDetailsService(IUserSecurityService);
        dap.setPasswordEncoder(passwordEncoder());

        return dap;
    }

    /*
     * Theo mặc định, Spring OAuth2 sử dụng
     * HttpSessionOAuth2AuthorizationRequestRepository để lưu
     * yêu cầu ủy quyền. Tuy nhiên, vì dịch vụ không có trạng thái nên không thể lưu
     * nó trong
     * phiên họp. Thay vào đó, sẽ lưu yêu cầu trong cookie được mã hóa Base64.
     */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Bật CORS đúng cách
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.addAllowedOrigin(Endpoints.front_end_host);
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                    configuration.addAllowedHeader("*");
                    configuration.setAllowCredentials(true);
                    configuration.setMaxAge(3600L);
                    return configuration;
                }))
                // Tắt CSRF nếu dùng JWT
                .csrf(csrf -> csrf.disable())
                // cấu hình phân quyền
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, Endpoints.PUBLIC_GET_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                // Xử lý lỗi 403
                .exceptionHandling(handling -> handling.accessDeniedHandler(customAccessDeniedHandler))
                // Dùng JWT filter
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                // Session cho JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Basic Authentication Entry Point (nếu dùng)
                // Tùy chọn cách xác thực ngoại lệ đăng nhập
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(customBasicAuthenticationEntryPoint))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize") // URL endpoint xác thực OAuth2
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository()) // Repository
                                                                                                        // cho request
                                                                                                        // OAuth2
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*") // URL callback sau khi xác thực
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Dịch vụ lấy thông tin người dùng OAuth2
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler))
                // Nếu không phải đăng nhập bằng oauth thì cái nay lấy thông tin người dùng
                .userDetailsService(userSecurityService);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); //
    }
}
