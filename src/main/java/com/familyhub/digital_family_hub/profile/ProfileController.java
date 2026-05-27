package com.familyhub.digital_family_hub.profile;

import com.familyhub.digital_family_hub.auth.AuthDTO;
import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping("/me")
    public ApiResponse<AuthDTO.CurrentUserResponse> updateCurrentUser(
        @Valid @RequestBody ProfileDTO.UpdateRequest request,
        Principal principal
    ) {
        return ApiResponse.ok(profileService.updateCurrentUser(request, principal));
    }
}
