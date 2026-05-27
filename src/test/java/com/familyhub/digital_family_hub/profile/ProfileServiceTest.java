package com.familyhub.digital_family_hub.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familyhub.digital_family_hub.auth.AuthDTO;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import com.familyhub.digital_family_hub.users.UserRole;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProfileServiceTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final ProfileService service = new ProfileService(users);

    @Test
    void updateCurrentUserOnlyChangesEditableProfileFields() {
        AppUser currentUser = user(UserRole.VIEWER);
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthDTO.CurrentUserResponse response = service.updateCurrentUser(
            new ProfileDTO.UpdateRequest("  New Name  ", "  https://example.com/avatar.png  "),
            principal(currentUser.getEmail())
        );

        assertThat(response.id()).isEqualTo(currentUser.getId());
        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.avatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.email()).isEqualTo(currentUser.getEmail());
        assertThat(response.role()).isEqualTo(UserRole.VIEWER.name());
        assertThat(currentUser.getRole()).isEqualTo(UserRole.VIEWER);
        verify(users).save(currentUser);
    }

    @Test
    void updateCurrentUserClearsBlankAvatarUrl() {
        AppUser currentUser = user(UserRole.MEMBER);
        currentUser.setAvatarUrl("https://example.com/old.png");
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthDTO.CurrentUserResponse response = service.updateCurrentUser(
            new ProfileDTO.UpdateRequest("Family Member", "   "),
            principal(currentUser.getEmail())
        );

        assertThat(response.avatarUrl()).isNull();
    }

    @Test
    void updateCurrentUserRejectsBlankDisplayName() {
        AppUser currentUser = user(UserRole.MEMBER);
        when(users.findByEmailIgnoreCase(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> service.updateCurrentUser(
            new ProfileDTO.UpdateRequest("   ", null),
            principal(currentUser.getEmail())
        ))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(400)
            );
    }

    @Test
    void updateCurrentUserRequiresAuthentication() {
        assertThatThrownBy(() -> service.updateCurrentUser(new ProfileDTO.UpdateRequest("Family Member", null), null))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(401)
            );
    }

    private Principal principal(String email) {
        return () -> email;
    }

    private AppUser user(UserRole role) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("member@example.com");
        user.setName("Family Member");
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setRole(role);
        return user;
    }
}
