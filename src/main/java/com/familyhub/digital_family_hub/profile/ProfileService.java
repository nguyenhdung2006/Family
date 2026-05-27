package com.familyhub.digital_family_hub.profile;

import com.familyhub.digital_family_hub.auth.AuthDTO;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final AppUserRepository users;

    public ProfileService(AppUserRepository users) {
        this.users = users;
    }

    @Transactional
    public AuthDTO.CurrentUserResponse updateCurrentUser(ProfileDTO.UpdateRequest request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        String displayName = request.displayName().trim();
        if (displayName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name is required");
        }
        currentUser.setName(displayName);
        currentUser.setAvatarUrl(trimToNull(request.avatarUrl()));
        return currentUserResponse(users.save(currentUser));
    }

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private AuthDTO.CurrentUserResponse currentUserResponse(AppUser user) {
        UserRole role = user.getRole();
        return new AuthDTO.CurrentUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatarUrl(),
            role.name(),
            List.of(role.name())
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
