package com.familyhub.digital_family_hub.auth;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.shared.audit.AuditLogService;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final AuditLogService auditLogService;

    public AuthService(AppUserRepository users, AuditLogService auditLogService) {
        this.users = users;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthDTO.CurrentUserResponse currentUser(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            return upsertOAuthUser(oauthToken);
        }
        if (principal != null) {
            return new AuthDTO.CurrentUserResponse(null, principal.getName(), null, null);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    public AuthDTO.LoginOptionsResponse loginOptions() {
        return new AuthDTO.LoginOptionsResponse(Map.of(
            "googleOAuthPath", "/oauth2/authorization/google",
            "sessionProbePath", "/api/auth/me"
        ));
    }

    private AuthDTO.CurrentUserResponse upsertOAuthUser(OAuth2AuthenticationToken oauthToken) {
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth profile did not include an email");
        }

        AppUser user = users.findByEmailIgnoreCase(email).orElseGet(AppUser::new);
        user.setEmail(email);
        user.setName(firstNonBlank(oauthUser.getAttribute("name"), email));
        user.setAvatarUrl(oauthUser.getAttribute("picture"));
        AppUser saved = users.save(user);
        auditLogService.login(saved.getEmail());

        return new AuthDTO.CurrentUserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAvatarUrl());
    }

    private String firstNonBlank(String value, String fallback) {
        return Optional.ofNullable(value).filter(text -> !text.isBlank()).orElse(fallback);
    }
}
