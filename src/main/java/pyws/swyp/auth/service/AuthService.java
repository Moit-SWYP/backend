package pyws.swyp.auth.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.SignupRequest;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

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
        return new AuthResponse(false, null);
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        LoginRequest login = request.login();  // 소셜 로그인 정보

        // 동일한 소셜 계정 검증
        socialAccountRepository.findBySocialProviderAndSocialId(
                login.socialProvider(),
                login.socialId()
        ).ifPresent(socialAccount -> {
            throw new CustomException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS);
        });

        // 회원 생성
        Member member = Member.builder()
                .email(login.email())
                .nickname(request.nickname())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .build();
        memberRepository.save(member);

        // 소셜 계정 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .socialProvider(login.socialProvider())
                .socialId(login.socialId())
                .member(member)
                .build();
        socialAccountRepository.save(socialAccount);

        return new AuthResponse(false, null);
    }
}
