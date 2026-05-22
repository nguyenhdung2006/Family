package com.familyhub.digital_family_hub.auth;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ApiResponse<AuthDTO.CurrentUserResponse> currentUser(Principal principal) {
        return ApiResponse.ok(authService.currentUser(principal));
    }

    @GetMapping("/login-options")
    public ApiResponse<AuthDTO.LoginOptionsResponse> loginOptions() {
        return ApiResponse.ok(authService.loginOptions());
    }
}
