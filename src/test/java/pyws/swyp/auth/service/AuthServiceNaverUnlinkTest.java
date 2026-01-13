package pyws.swyp.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pyws.swyp.auth.oauth.NaverUnlinkCrypto;
import pyws.swyp.auth.oauth.OAuthProperties;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.repository.SocialAccountRepository;
import pyws.swyp.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class AuthServiceNaverUnlinkTest {

    @Mock
    OAuthProperties oAuthProperties;
    @Mock
    OAuthProperties.Naver naverProps;
    @Mock
    SocialAccountRepository socialAccountRepository;
    @Mock
    MemberService memberService;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("clientId가 다르면 바로 return, crypto/DB/withdraw/delete 호출 없음")
    void invalidClientId_returnsEarly() {
        // given
        given(oAuthProperties.naver()).willReturn(naverProps);
        given(naverProps.clientId()).willReturn("expected-client");

        // when
        authService.handleNaverUnlinkCallback("wrong-client", "enc", "ts", "sig");

        // then
        verifyNoInteractions(socialAccountRepository);
        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("signature가 다르면 return, decrypt/DB/withdraw/delete 호출 없음")
    void invalidSignature_returnsEarly() {
        // given
        String clientId = "client";
        String encryptUniqueId = "enc";
        String timestamp = "1693877406";
        String signature = "received-sig";

        given(oAuthProperties.naver()).willReturn(naverProps);
        given(naverProps.clientId()).willReturn(clientId);
        given(naverProps.clientSecret()).willReturn("secret");

        try (MockedStatic<NaverUnlinkCrypto> crypto = Mockito.mockStatic(NaverUnlinkCrypto.class)) {
            byte[] key = new byte[]{1, 2, 3};

            crypto.when(() -> NaverUnlinkCrypto.generateKey("secret")).thenReturn(key);
            crypto.when(() -> NaverUnlinkCrypto.signatureBaseString(clientId, encryptUniqueId, timestamp))
                    .thenReturn("base");
            crypto.when(() -> NaverUnlinkCrypto.generateMac("base", key))
                    .thenReturn("generated-sig");
            crypto.when(() -> NaverUnlinkCrypto.constantTimeEquals("generated-sig", signature))
                    .thenReturn(false);

            // when
            authService.handleNaverUnlinkCallback(clientId, encryptUniqueId, timestamp, signature);

            // then
            crypto.verify(() -> NaverUnlinkCrypto.decryptUniqueId(anyString(), any()), never());
            verifyNoInteractions(socialAccountRepository);
            verifyNoInteractions(memberService);
        }
    }

    @Test
    @DisplayName("복호화 성공했지만 SocialAccount 없으면 return, withdraw/delete 호출 없음")
    void socialAccountNotFound_returnsEarly() {
        // given
        String clientId = "client";
        String encryptUniqueId = "enc";
        String timestamp = "1693877406";
        String signature = "sig";

        given(oAuthProperties.naver()).willReturn(naverProps);
        given(naverProps.clientId()).willReturn(clientId);
        given(naverProps.clientSecret()).willReturn("secret");

        try (MockedStatic<NaverUnlinkCrypto> crypto = Mockito.mockStatic(NaverUnlinkCrypto.class)) {
            byte[] key = new byte[]{1, 2, 3};

            crypto.when(() -> NaverUnlinkCrypto.generateKey("secret")).thenReturn(key);
            crypto.when(() -> NaverUnlinkCrypto.signatureBaseString(clientId, encryptUniqueId, timestamp))
                    .thenReturn("base");
            crypto.when(() -> NaverUnlinkCrypto.generateMac("base", key)).thenReturn("generated");
            crypto.when(() -> NaverUnlinkCrypto.constantTimeEquals("generated", signature)).thenReturn(true);
            crypto.when(() -> NaverUnlinkCrypto.decryptUniqueId(encryptUniqueId, key)).thenReturn("unique-id");

            given(socialAccountRepository.findBySocialProviderAndSocialId(SocialProvider.NAVER, "unique-id"))
                    .willReturn(Optional.empty());

            // when
            authService.handleNaverUnlinkCallback(clientId, encryptUniqueId, timestamp, signature);

            // then
            verifyNoInteractions(memberService);
            verify(socialAccountRepository, never()).delete(any());
        }
    }

    @Test
    @DisplayName("모든 검증 통과 + linkedCount>=2면 SocialAccount만 삭제한다")
    void success_whenMultipleLinkedAccounts_deletesSocialAccountOnly() {
        // given
        String clientId = "client";
        String encryptUniqueId = "enc";
        String timestamp = "1693877406";
        String signature = "sig";

        Member member = mock(Member.class);
        given(member.getId()).willReturn(10L);

        SocialAccount socialAccount = mock(SocialAccount.class);
        given(socialAccount.getMember()).willReturn(member);

        given(oAuthProperties.naver()).willReturn(naverProps);
        given(naverProps.clientId()).willReturn(clientId);
        given(naverProps.clientSecret()).willReturn("secret");

        try (MockedStatic<NaverUnlinkCrypto> crypto = Mockito.mockStatic(NaverUnlinkCrypto.class)) {
            byte[] key = new byte[]{1, 2, 3};

            crypto.when(() -> NaverUnlinkCrypto.generateKey("secret")).thenReturn(key);
            crypto.when(() -> NaverUnlinkCrypto.signatureBaseString(clientId, encryptUniqueId, timestamp))
                    .thenReturn("base");
            crypto.when(() -> NaverUnlinkCrypto.generateMac("base", key)).thenReturn("generated");
            crypto.when(() -> NaverUnlinkCrypto.constantTimeEquals("generated", signature)).thenReturn(true);
            crypto.when(() -> NaverUnlinkCrypto.decryptUniqueId(encryptUniqueId, key)).thenReturn("unique-id");

            given(socialAccountRepository.findBySocialProviderAndSocialId(SocialProvider.NAVER, "unique-id"))
                    .willReturn(Optional.of(socialAccount));
            given(socialAccountRepository.countByMemberId(10L)).willReturn(2L);

            // when
            authService.handleNaverUnlinkCallback(clientId, encryptUniqueId, timestamp, signature);

            // then
            verify(socialAccountRepository).delete(socialAccount);
            verify(memberService, never()).withdrawByUnlinkCallback(anyLong());
        }
    }

    @Test
    @DisplayName("모든 검증 통과 + linkedCount==1이면 회원 탈퇴")
    void success_whenSingleLinkedAccount_withdrawsMember() {
        // given
        String clientId = "client";
        String encryptUniqueId = "enc";
        String timestamp = "1693877406";
        String signature = "sig";

        Member member = mock(Member.class);
        given(member.getId()).willReturn(10L);

        SocialAccount socialAccount = mock(SocialAccount.class);
        given(socialAccount.getMember()).willReturn(member);

        given(oAuthProperties.naver()).willReturn(naverProps);
        given(naverProps.clientId()).willReturn(clientId);
        given(naverProps.clientSecret()).willReturn("secret");

        try (MockedStatic<NaverUnlinkCrypto> crypto = Mockito.mockStatic(NaverUnlinkCrypto.class)) {
            byte[] key = new byte[]{1, 2, 3};

            crypto.when(() -> NaverUnlinkCrypto.generateKey("secret")).thenReturn(key);
            crypto.when(() -> NaverUnlinkCrypto.signatureBaseString(clientId, encryptUniqueId, timestamp))
                    .thenReturn("base");
            crypto.when(() -> NaverUnlinkCrypto.generateMac("base", key)).thenReturn("generated");
            crypto.when(() -> NaverUnlinkCrypto.constantTimeEquals("generated", signature)).thenReturn(true);
            crypto.when(() -> NaverUnlinkCrypto.decryptUniqueId(encryptUniqueId, key)).thenReturn("unique-id");

            given(socialAccountRepository.findBySocialProviderAndSocialId(SocialProvider.NAVER, "unique-id"))
                    .willReturn(Optional.of(socialAccount));
            given(socialAccountRepository.countByMemberId(10L)).willReturn(1L);

            // when
            authService.handleNaverUnlinkCallback(clientId, encryptUniqueId, timestamp, signature);

            // then
            verify(memberService).withdrawByUnlinkCallback(10L);
            verify(socialAccountRepository, never()).delete(any());
        }
    }
}
