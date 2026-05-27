package com.familyhub.digital_family_hub.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProfileDTO {

    private ProfileDTO() {
    }

    public record UpdateRequest(
        @NotBlank @Size(max = 255) String displayName,
        @Size(max = 2000) String avatarUrl
    ) {
    }
}
