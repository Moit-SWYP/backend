package pyws.swyp.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialAccountInfo;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberWithdrawal;
import pyws.swyp.member.entity.Role;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.entity.WithdrawalType;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.MemberWithdrawalRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SocialAccountRepository socialAccountRepository;

    @Autowired
    MemberWithdrawalRepository memberWithdrawalRepository;

    @MockitoBean
    JwtService jwtService;

    private Member savedMember;
    private List<SocialAccount> savedSocialAccounts;

    @BeforeEach
    void setUp() {
        memberWithdrawalRepository.deleteAll();
        socialAccountRepository.deleteAll();
        memberRepository.deleteAll();

        this.savedMember = memberRepository.save(Member.builder()
                .email("test@example.com")
                .nickname("테스트")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .role(Role.MEMBER)
                .build());

        List<SocialAccount> socialAccounts = List.of(
                SocialAccount.builder()
                        .member(savedMember)
                        .socialProvider(SocialProvider.KAKAO)
                        .socialId("kakao-1")
                        .build(),
                SocialAccount.builder()
                        .member(savedMember)
                        .socialProvider(SocialProvider.NAVER)
                        .socialId("naver-1")
                        .build()
        );

        socialAccountRepository.saveAll(socialAccounts);
        this.savedSocialAccounts = socialAccounts;
    }

    @Test
    @DisplayName("로그인 정보를 조회한다.")
    void getMe_success() {
        // when
        MemberResponse response = memberService.getMe(savedMember.getId());

        // then
        assertEquals("test@example.com", response.email());
        assertEquals("테스트", response.nickname());
        assertEquals(LocalDate.of(1999, 1, 1), response.birthDate());
        assertEquals(Gender.MALE, response.gender());
        assertEquals(Role.MEMBER, response.role());

        List<SocialProvider> providers = response.socialAccounts().stream()
                .map(SocialAccountInfo::socialProvider)
                .toList();

        assertEquals(savedSocialAccounts.size(), providers.size());
        assertTrue(providers.containsAll(List.of(SocialProvider.KAKAO, SocialProvider.NAVER)));
    }

    @Test
    @DisplayName("존재하지 않는 회원은 조회할 수 없다.")
    void getMe_memberNotFound() {
        assertThrows(CustomException.class, () -> memberService.getMe(9999L));
    }

    @Test
    @DisplayName("탈퇴 시 탈퇴사유 저장, 소셜 계정 삭제, 회원 삭제, 로그아웃 호출")
    void withdraw_success() {
        // given
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.BUG, null);

        // when
        memberService.withdraw(savedMember.getId(), request);

        // then
        assertTrue(memberRepository.findById(savedMember.getId()).isEmpty());
        assertEquals(0, socialAccountRepository.count());

        List<MemberWithdrawal> withdrawals = memberWithdrawalRepository.findAll();
        assertEquals(1, withdrawals.size());
        assertEquals(WithdrawalType.BUG, withdrawals.getFirst().getType());

        verify(jwtService, times(1)).logout(savedMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외 + 로그아웃 미호출")
    void withdraw_memberNotFound() {
        // given
        MemberWithdrawRequest request = new MemberWithdrawRequest(
                WithdrawalType.ETC,
                "테스트"
        );

        // expected
        assertThrows(CustomException.class, () -> memberService.withdraw(9999L, request));
        verify(jwtService, never()).logout(anyLong());
    }
}
