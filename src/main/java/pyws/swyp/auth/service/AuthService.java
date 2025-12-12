package pyws.swyp.auth.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.SignupRequest;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.exception.SocialAccountAlreadyExists;
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
                        request.getSocialProvider(),
                        request.getSocialId()
                );

        // 신규 회원 -> 회원가입 필요
        if (socialAccountOpt.isEmpty()) {
            return AuthResponse.builder()
                    .signupRequired(true)
                    .tokens(null)
                    .build();
        }

        // 기존 회원 -> JWT 발급
        return AuthResponse.builder()
                .signupRequired(false)
                .tokens(null)  // todo
                .build();
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        LoginRequest login = request.getLogin();  // 소셜 로그인 정보

        // 동일한 소셜 계정 검증
        socialAccountRepository.findBySocialProviderAndSocialId(
                login.getSocialProvider(),
                login.getSocialId()
        ).ifPresent(socialAccount -> {
            throw new SocialAccountAlreadyExists();
        });

        // 회원 생성
        Member member = Member.builder()
                .email(login.getEmail())
                .nickname(request.getNickname())
                .birthDate(request.getBirthday())
                .gender(request.getGender())
                .build();
        memberRepository.save(member);

        // 소셜 계정 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .socialProvider(login.getSocialProvider())
                .socialId(login.getSocialId())
                .member(member)
                .build();
        socialAccountRepository.save(socialAccount);

        return AuthResponse.builder()
                .signupRequired(false)
                .tokens(null)  // todo
                .build();
    }
}
