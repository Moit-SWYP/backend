package pyws.swyp.auth.service;

import static pyws.swyp.global.error.ErrorCode.MEMBER_NOT_FOUND;
import static pyws.swyp.global.error.ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.JwtResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.SignupRequest;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.Role;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtService jwtService;

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
                .role(Role.MEMBER)
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
}
