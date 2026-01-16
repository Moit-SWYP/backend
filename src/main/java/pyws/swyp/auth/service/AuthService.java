package pyws.swyp.auth.service;

import static pyws.swyp.global.error.ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.JwtResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.SignupRequest;
import pyws.swyp.auth.oauth.NaverUnlinkCrypto;
import pyws.swyp.auth.oauth.OAuthProperties;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.SocialAccountRepository;
import pyws.swyp.member.service.MemberService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtService jwtService;
    private final OAuthProperties oAuthProperties;
    private final MemberService memberService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        Optional<SocialAccount> socialAccountOpt =
                socialAccountRepository.findBySocialProviderAndSocialId(
                        request.socialProvider(),
                        request.socialId()
                );

        // 신규 회원 -> 회원가입 필요
        if (socialAccountOpt.isEmpty()) {
            return new AuthResponse(true, null);
        }

        // 기존 회원 -> JWT 발급
        Member member = socialAccountOpt.get().getMember();
        JwtResponse tokens = jwtService.issueTokens(member.getId(), member.getRole());

        return new AuthResponse(false, tokens);
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        LoginRequest login = request.login();  // 소셜 로그인 정보

        // 동일한 소셜 계정 검증
        socialAccountRepository.findBySocialProviderAndSocialId(
                login.socialProvider(),
                login.socialId()
        ).ifPresent(socialAccount -> {
            throw SOCIAL_ACCOUNT_ALREADY_EXISTS.toException();
        });

        // 회원 생성
        Member member = Member.builder()
                .email(login.email())
                .nickname(request.nickname())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .role(MemberRole.MEMBER)
                .characterType(request.characterType())
                .build();
        memberRepository.save(member);

        // 소셜 계정 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .socialProvider(login.socialProvider())
                .socialId(login.socialId())
                .member(member)
                .build();
        socialAccountRepository.save(socialAccount);

        // 로그인 처리
        JwtResponse tokens = jwtService.issueTokens(member.getId(), member.getRole());

        return new AuthResponse(false, tokens);
    }

    public void logout(Long memberId) {
        jwtService.logout(memberId);
    }

    @Transactional
    public void handleKakaoUnlinkCallback(String authorization, String appId, String userId, String referrerType) {
        // Authorization 헤더 검증
        String expectedAuth = "KakaoAK " + oAuthProperties.kakao().adminKey();
        if (!expectedAuth.equals(authorization)) {
            log.warn("Invalid Kakao Authorization header.");
            return;
        }

        // appId 검증
        if (!oAuthProperties.kakao().appId().equals(appId)) {
            log.warn("Invalid Kakao appId. received={}", appId);
            return;
        }

        SocialAccount socialAccount = socialAccountRepository
                .findBySocialProviderAndSocialId(SocialProvider.KAKAO, userId)
                .orElse(null);

        if (socialAccount == null) {
            log.warn("SocialAccount not found.");
            return;
        }

        processUnlink(socialAccount);
    }

    @Transactional
    public void handleNaverUnlinkCallback(String clientId, String encryptUniqueId, String timestamp, String signature) {
        // clientId 검증
        if (!oAuthProperties.naver().clientId().equals(clientId)) {
            log.warn("Invalid Naver clientId. received={}", clientId);
            return;
        }

        // HMAC 검증
        byte[] key = NaverUnlinkCrypto.generateKey(oAuthProperties.naver().clientSecret());
        String baseString = NaverUnlinkCrypto.signatureBaseString(clientId, encryptUniqueId, timestamp);
        String generated = NaverUnlinkCrypto.generateMac(baseString, key);

        if (!NaverUnlinkCrypto.constantTimeEquals(generated, signature)) {
            log.warn("Invalid Naver unlink signature.");
            return;
        }

        // encryptUniqueId 복호화
        String uniqueId = NaverUnlinkCrypto.decryptUniqueId(encryptUniqueId, key);

        // DB 소셜 계정 조회
        SocialAccount socialAccount = socialAccountRepository
                .findBySocialProviderAndSocialId(SocialProvider.NAVER, uniqueId)
                .orElse(null);

        if (socialAccount == null) {
            log.warn("SocialAccount not found.");
            return;
        }

        processUnlink(socialAccount);
    }

    private void processUnlink(SocialAccount socialAccount) {
        Long memberId = socialAccount.getMember().getId();
        long linkedCount = socialAccountRepository.countByMemberId(memberId);

        // 소셜 계정이 2개 이상일 경우 연동된 소셜 계정만 제거
        if (linkedCount >= 2) {
            socialAccountRepository.delete(socialAccount);
            return;
        }

        // 유일한 소셜 계정일 경우 회원 탈퇴
        memberService.withdrawByUnlinkCallback(memberId);
    }
}
