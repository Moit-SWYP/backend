package pyws.swyp.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.auth.controller.api.AuthApi;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.JwtResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.ReissueRequest;
import pyws.swyp.auth.dto.SignupRequest;
import pyws.swyp.auth.service.AuthService;
import pyws.swyp.auth.service.JwtService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Validated LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody @Validated SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal Long memberId) {
        authService.logout(memberId);
    }

    @PostMapping("/reissue")
    public JwtResponse reissue(@RequestBody @Validated ReissueRequest request) {
        return jwtService.reissue(request.refreshToken());
    }

    @PostMapping("/oauth/kakao/unlink")
    public ResponseEntity<Void> handleKakaoUnlinkCallback(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("app_id") String appId,
            @RequestParam("user_id") String userId,
            @RequestParam(value = "referrer_type") String referrerType
    ) {
        authService.handleKakaoUnlinkCallback(authorization, appId, userId, referrerType);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/oauth/naver/unlink")
    public ResponseEntity<Void> handleNaverUnlinkCallback(
            @RequestParam("clientId") String clientId,
            @RequestParam("encryptUniqueId") String encryptUniqueId,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("signature") String signature
    ) {
        authService.handleNaverUnlinkCallback(clientId, encryptUniqueId, timestamp, signature);
        return ResponseEntity.noContent().build();
    }
}
