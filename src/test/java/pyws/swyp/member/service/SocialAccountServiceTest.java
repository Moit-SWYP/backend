package pyws.swyp.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.member.dto.SocialLinkRequest;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.Role;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@SpringBootTest
class SocialAccountServiceTest {

    @Autowired
    SocialAccountService socialAccountService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SocialAccountRepository socialAccountRepository;

    @MockitoBean
    JwtService jwtService;

    private Member savedMember;

    @BeforeEach
    void setUp() {
        socialAccountRepository.deleteAll();
        memberRepository.deleteAll();

        this.savedMember = memberRepository.save(Member.builder()
                .email("test@example.com")
                .nickname("테스트")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .role(Role.MEMBER)
                .build());
    }

    @Test
    @DisplayName("소셜 계정 연동에 성공한다")
    void link_success() {
        // given
        String socialId = "social-id-123";
        SocialLinkRequest request = new SocialLinkRequest(SocialProvider.KAKAO, socialId);

        // when
        socialAccountService.link(savedMember.getId(), request);

        // then
        assertEquals(1, socialAccountRepository.count());

        SocialAccount socialAccount = socialAccountRepository.findAll().getFirst();
        assertEquals(SocialProvider.KAKAO, socialAccount.getSocialProvider());
        assertEquals(socialId, socialAccount.getSocialId());
        assertEquals(savedMember.getId(), socialAccount.getMember().getId());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 MEMBER_NOT_FOUND 예외가 발생한다")
    void link_memberNotFound() {
        // given
        Long memberId = savedMember.getId() + 999999;
        SocialLinkRequest request = new SocialLinkRequest(SocialProvider.KAKAO, "social-id-123");

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> socialAccountService.link(memberId, request));

        // then
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("이미 존재하는 소셜 계정이면 SOCIAL_ACCOUNT_ALREADY_EXISTS 예외가 발생한다")
    void link_alreadyExists() {
        // given
        String socialId = "social-id-123";
        SocialAccount socialAccount = SocialAccount.builder()
                .socialProvider(SocialProvider.KAKAO)
                .socialId(socialId)
                .member(savedMember)
                .build();
        socialAccountRepository.save(socialAccount);

        SocialLinkRequest request = new SocialLinkRequest(SocialProvider.KAKAO, socialId);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> socialAccountService.link(savedMember.getId(), request));

        // then
        assertEquals(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS, ex.getErrorCode());
        assertEquals(1, socialAccountRepository.count());
    }


    @Test
    @DisplayName("소셜 계정 연동 해제에 성공한다")
    void unlink_success() {
        // given
        SocialAccount socialAccount = SocialAccount.builder()
                .socialProvider(SocialProvider.KAKAO)
                .socialId("social-id-123")
                .member(savedMember)
                .build();
        socialAccountRepository.save(socialAccount);

        // when
        socialAccountService.unlink(savedMember.getId(), SocialProvider.KAKAO);

        // then
        assertEquals(0, socialAccountRepository.count());
    }

    @Test
    @DisplayName("연동된 소셜 계정이 없으면 SOCIAL_ACCOUNT_FOUND 예외가 발생한다")
    void unlink_notFound() {
        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> socialAccountService.unlink(savedMember.getId(), SocialProvider.KAKAO));

        // then
        assertEquals(ErrorCode.SOCIAL_ACCOUNT_FOUND, ex.getErrorCode());
    }
}