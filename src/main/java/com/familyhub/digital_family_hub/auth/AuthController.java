package com.familyhub.digital_family_hub.auth;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthLogoutHandler logoutHandler;

    public AuthController(AuthService authService, AuthLogoutHandler logoutHandler) {
        this.authService = authService;
        this.logoutHandler = logoutHandler;
    }

    @GetMapping("/me")
    public ApiResponse<AuthDTO.CurrentUserResponse> currentUser(Principal principal) {
        return ApiResponse.ok(authService.currentUser(principal));
    }

    @GetMapping("/login-options")
    public ApiResponse<AuthDTO.LoginOptionsResponse> loginOptions() {
        return ApiResponse.ok(authService.loginOptions());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        return logoutHandler.logout(request);
    }
}
