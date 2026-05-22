package com.familyhub.digital_family_hub.auth;

import java.util.Map;
import java.util.UUID;

public final class AuthDTO {

    private AuthDTO() {
    }

    public record CurrentUserResponse(UUID id, String name, String email, String avatarUrl) {
    }

    public record LoginOptionsResponse(Map<String, Object> options) {
    }
}
