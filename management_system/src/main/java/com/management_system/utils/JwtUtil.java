
package com.management_system.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.management_system.entity.Role;
import com.management_system.entity.User;
import com.management_system.repository.RoleRepository;
import com.management_system.service.inter.IUserSecurityService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    public static final String SECRET_ACCESS_TOKEN = "dsafwerwdrwerwer01234567894234234rfsdfsdfsdfsdvfuyuytuytu0123456789";
    public static final String SECRET_REFRESH_TOKEN = "EUOIQWEUQWEJQOWIDKJASDNASDNM1231290KCMASKMDQSLKMDK323I912I3KMCADASD";
    private final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000; // 2 hours
    private final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IUserSecurityService iUserSecurityService;

    public String generateToken(String userName) {
        Map<String, Object> claim = new HashMap<>();
        User user = iUserSecurityService.findByEmail(userName);

        claim.put("userId", user.getId());
        claim.put("type", "accessToken");

        boolean isAdmin = false;
        boolean isPm = false;
        boolean isStaff = false;
        boolean isUser = false;

        Role role = roleRepository.findByIdAndDeleteFlagFalse(user.getRoleId())
                .orElse(null);

        if (role != null) {
            switch (role.getName()) {
                case "ROLE_ADMIN":
                    isAdmin = true;
                    break;
                case "ROLE_PM":
                    isPm = true;
                    break;
                case "ROLE_STAFF":
                    isStaff = true;
                    break;
                case "ROLE_USER":
                    isUser = true;
                    break;
                default:
            }
        }
        if (role != null) {
            claim.put("role", role.getName());
        }
        claim.put("isAdmin", isAdmin);
        claim.put("isPm", isPm);
        claim.put("isStaff", isStaff);
        claim.put("isUser", isUser);

        return createToken(claim, userName, ACCESS_TOKEN_EXPIRATION, SECRET_ACCESS_TOKEN);
    }

    private String createToken(Map<String, Object> claim, String userName, long expiration, String secret) {
        return Jwts.builder()
                .setClaims(claim)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refreshToken");
        return createToken(claims, username, REFRESH_TOKEN_EXPIRATION, SECRET_REFRESH_TOKEN);
    }

    private Key getSignKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token, String secret) {
        return Jwts.parser().setSigningKey(getSignKey(secret)).parseClaimsJws(token).getBody();
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsFunction, String secret) {
        Claims claims = extractAllClaims(token, secret);
        return claimsFunction.apply(claims);
    }

    public Date extractExpiration(String token, String secret) {
        try {
            return extractClaims(token, Claims::getExpiration, secret);
        } catch (JwtException e) {
            throw new RuntimeException("Failed to extract expiration from JWT: " + e.getMessage());
        }
    }

    public String extractUserName(String token, String secret) {
        return extractClaims(token, Claims::getSubject, secret);
    }

    private Boolean isTokenExpired(String token, String secret) {
        Date expiration = extractExpiration(token, secret);
        if (expiration == null) {
            throw new RuntimeException("Expiration date is null");
        }
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails, String secret) {
        String userName = extractUserName(token, secret);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token, secret));
    }

    public Boolean validateRefreshToken(String refreshToken, String secret) {
        try {
            extractAllClaims(refreshToken, secret);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}