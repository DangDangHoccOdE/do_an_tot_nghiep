package com.management_system.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.management_system.entity.User;
import com.management_system.repository.RoleRepository;
import com.management_system.service.inter.IUserSecurityService;
import com.management_system.utils.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver resolver;

    @Autowired
    private JwtUtil jwtService;

    @Autowired
    private IUserSecurityService iUserSecurityService;

    @Autowired
    private RoleRepository roleRepository;

    public JwtFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                authHeader = request.getHeader("X-Refresh-Token");
            }
            String token = null;
            String username = null;
            String tokenType = null;
            if (authHeader != null && (authHeader.startsWith("Bearer ") || authHeader.startsWith("Refresh-Token"))) {
                token = authHeader.substring(authHeader.startsWith("Bearer ") ? 7 : 14);
                tokenType = authHeader.startsWith("Bearer ") ? JwtUtil.SECRET_ACCESS_TOKEN : JwtUtil.SECRET_REFRESH_TOKEN;

                username = jwtService.extractUserName(token, tokenType);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;

                User user = iUserSecurityService.findByEmail(username);

                String role = roleRepository.findByIdAndDeleteFlagFalse(user.getRoleId())
                        .map(r -> r.getName())
                        .orElse("ROLE_USER");

                if (user != null && user.getProvider() != null) { // OAuth2 user logic
                    userDetails = new org.springframework.security.core.userdetails.User(
                            user.getEmail(),
                            "", // Password không có
                            List.of(new SimpleGrantedAuthority(role))
                    );
                } else {
                    // Xử lý user
                    userDetails = iUserSecurityService.loadUserByUsername(username);
                }

                boolean isTokenValid = jwtService.validateToken(token, userDetails, tokenType);
                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            resolver.resolveException(request, response, null, e);
        }
    }
    
}
