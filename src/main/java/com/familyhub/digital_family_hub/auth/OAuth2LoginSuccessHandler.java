package com.familyhub.digital_family_hub.auth;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository users;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final String successRedirect;
    private final boolean secureCookies;

    public OAuth2LoginSuccessHandler(
        AppUserRepository users,
        JwtService jwtService,
        AuditLogService auditLogService,
        @Value("${hometree.auth.oauth-success-redirect}") String successRedirect,
        @Value("${hometree.auth.secure-cookies}") boolean secureCookies
    ) {
        this.users = users;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.successRedirect = successRedirect;
        this.secureCookies = secureCookies;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth profile did not include an email");
            return;
        }

        AppUser user = users.findByEmailIgnoreCase(email).orElseGet(AppUser::new);
        user.setEmail(email);
        user.setName(firstNonBlank(oauthUser.getAttribute("name"), email));
        user.setAvatarUrl(oauthUser.getAttribute("picture"));
        AppUser saved = users.save(user);
        auditLogService.login(saved.getEmail());

        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.TOKEN_COOKIE, jwtService.issueToken(saved))
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofHours(2))
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect(successRedirect);
    }

    private String firstNonBlank(String value, String fallback) {
        return Optional.ofNullable(value).filter(text -> !text.isBlank()).orElse(fallback);
    }
}
