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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUserName(token, JwtUtil.SECRET_ACCESS_TOKEN);

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
                            List.of(new SimpleGrantedAuthority(role)));
                } else {
                    // Xử lý user
                    userDetails = iUserSecurityService.loadUserByUsername(username);
                }

                boolean isTokenValid = jwtService.validateToken(token, userDetails, JwtUtil.SECRET_ACCESS_TOKEN);
                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            // Khi JWT hết hạn, trả về 401 để frontend có thể gọi refresh token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"JWT expired\",\"error\":\"token_expired\"}");
            response.getWriter().flush();
            // KHÔNG gọi filterChain.doFilter() - dừng xử lý tại đây
            return;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/roles/get-all");
    }

}
