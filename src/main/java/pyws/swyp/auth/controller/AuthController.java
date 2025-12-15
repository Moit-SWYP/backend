package pyws.swyp.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.auth.controller.api.AuthApi;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.SignupRequest;
import pyws.swyp.auth.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Validated LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout() {

    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody @Validated SignupRequest request) {
        return authService.signup(request);
    }
}
