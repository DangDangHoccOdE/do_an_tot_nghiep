package com.management_system.oauth2;

import static com.management_system.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.management_system.config.AppProperties;
import com.management_system.entity.User;
import com.management_system.exception.BusinessLogicException;
import com.management_system.service.impl.UserService;
import com.management_system.utils.CookieUtil;
import com.management_system.utils.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Lớp OAuth2AuthenticationSuccessHandler thực hiện các chức năng chính sau:
//Xử lý sự kiện xác thực thành công bằng cách tạo JWT tokens và chuyển hướng người dùng.
//Xóa các thuộc tính xác thực và cookie liên quan để dọn dẹp sau khi xác thực.
//Kiểm tra tính hợp lệ của redirect URI để đảm bảo chỉ chuyển hướng đến các URI hợp lệ.
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;
    private final UserService userService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired
    OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, AppProperties appProperties,
            @Lazy UserService userService,
            HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.jwtUtil = jwtUtil;
        this.appProperties = appProperties;
        this.userService = userService;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Phản hồi đã được cam kết. Không thể chuyển hướng đến " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        // Check xem cookie có redirect_url k
        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BusinessLogicException(
                    "Lấy làm tiếc! Chúng tôi có URI chuyển hướng trái phép và không thể tiến hành xác thực");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        final String accessToken = jwtUtil.generateToken(authentication.getName());
        final String refreshToken = jwtUtil.generateRefreshToken(authentication.getName());

        User user = userService.findUserByEmail(authentication.getName());

        user.setRefreshToken(refreshToken);
        userService.saveUser(user);

        // ĐƯờng dẫn trả về về là
        // http://localhost:3000/oauth2/redirect?accessToken=...&refreshToken=...
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Chỉ xác nhận máy chủ và cổng
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
}
