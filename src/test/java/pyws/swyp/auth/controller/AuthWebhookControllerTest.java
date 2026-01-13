package pyws.swyp.auth.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pyws.swyp.auth.service.AuthService;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.global.jwt.JwtProvider;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthWebhookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    @DisplayName("카카오 unlink 콜백 - 정상 요청이면 200 OK")
    void kakaoUnlink_ok() throws Exception {
        mockMvc.perform(post("/api/auth/oauth/kakao/unlink")
                        .header("Authorization", "KakaoAK test-admin-key")
                        .param("app_id", "123")
                        .param("user_id", "456")
                        .param("referrer_type", "UNLINK_FROM_APPS"))
                .andExpect(status().isOk());

        verify(authService).handleKakaoUnlinkCallback(
                "KakaoAK test-admin-key", "123", "456", "UNLINK_FROM_APPS"
        );
    }

    @Test
    @DisplayName("네이버 unlink 콜백 - 정상 요청이면 204 No Content")
    void naverUnlink_noContent() throws Exception {
        mockMvc.perform(post("/api/auth/oauth/naver/unlink")
                        .param("clientId", "naver-client-id")
                        .param("encryptUniqueId", "encrypted")
                        .param("timestamp", "1693877406")
                        .param("signature", "signature"))
                .andExpect(status().isNoContent());

        verify(authService).handleNaverUnlinkCallback(
                "naver-client-id", "encrypted", "1693877406", "signature"
        );
    }
}
