package com.familyhub.digital_family_hub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.UserRole;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void issuedTokenCanBeEncoded() {
        JwtService jwtService = new JwtService(new ObjectMapper(), "test-secret-that-is-long-enough", 30);
        AppUser user = testUser();

        String token = jwtService.issueToken(user);

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts)
            .allSatisfy(part -> assertThat(Base64.getUrlDecoder().decode(part)).isNotEmpty());
    }

    @Test
    void issuedTokenCanBeDecoded() {
        JwtService jwtService = new JwtService(new ObjectMapper(), "test-secret-that-is-long-enough", 30);
        AppUser user = testUser();

        String token = jwtService.issueToken(user);

        assertThat(jwtService.validate(token))
            .isPresent()
            .get()
            .satisfies(principal -> {
                assertThat(principal.email()).isEqualTo("member@example.com");
                assertThat(principal.name()).isEqualTo("Family Member");
                assertThat(principal.role()).isEqualTo(UserRole.MEMBER);
            });
    }

    @Test
    void expiredTokenCannotBeDecoded() {
        JwtService jwtService = new JwtService(new ObjectMapper(), "test-secret-that-is-long-enough", 0);
        AppUser user = testUser();

        String token = jwtService.issueToken(user);

        assertThat(jwtService.validate(token)).isEmpty();
    }

    private AppUser testUser() {
        AppUser user = new AppUser();
        user.setEmail("member@example.com");
        user.setName("Family Member");
        user.setRole(UserRole.MEMBER);
        return user;
    }
}
