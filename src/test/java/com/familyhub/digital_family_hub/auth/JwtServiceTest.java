package com.familyhub.digital_family_hub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.UserRole;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void issuedTokenCanBeValidated() {
        JwtService jwtService = new JwtService(new ObjectMapper(), "test-secret-that-is-long-enough", 30);
        AppUser user = new AppUser();
        user.setEmail("member@example.com");
        user.setName("Family Member");
        user.setRole(UserRole.MEMBER);

        String token = jwtService.issueToken(user);

        assertThat(jwtService.validate(token))
            .isPresent()
            .get()
            .satisfies(principal -> {
                assertThat(principal.email()).isEqualTo("member@example.com");
                assertThat(principal.role()).isEqualTo(UserRole.MEMBER);
            });
    }
}
