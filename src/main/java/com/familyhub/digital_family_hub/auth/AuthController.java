package com.familyhub.digital_family_hub.auth;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository users;

    public AuthController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            return upsertOAuthUser(oauthToken);
        }
        if (principal != null) {
            return new CurrentUserResponse(null, principal.getName(), null, null);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    private CurrentUserResponse upsertOAuthUser(OAuth2AuthenticationToken oauthToken) {
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

        return new CurrentUserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAvatarUrl());
    }

    private String firstNonBlank(String value, String fallback) {
        return Optional.ofNullable(value).filter(text -> !text.isBlank()).orElse(fallback);
    }

    public record CurrentUserResponse(UUID id, String name, String email, String avatarUrl) {
    }

    @GetMapping("/login-options")
    public Map<String, Object> loginOptions() {
        return Map.of(
            "googleOAuthPath", "/oauth2/authorization/google",
            "sessionProbePath", "/api/auth/me"
        );
    }
}
