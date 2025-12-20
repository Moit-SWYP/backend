package pyws.swyp.member.service;

import static pyws.swyp.global.error.ErrorCode.MEMBER_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialAccountInfo;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberWithdrawal;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.MemberWithdrawalRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::toException);

        List<SocialAccountInfo> socialAccounts =
                socialAccountRepository.findByMember(member).stream()
                        .map(sa -> new SocialAccountInfo(sa.getSocialProvider()))
                        .toList();

        return new MemberResponse(
                member.getEmail(),
                member.getNickname(),
                member.getBirthDate(),
                member.getGender(),
                member.getRole(),
                socialAccounts
        );
    }

    @Transactional
    public void withdraw(Long memberId, MemberWithdrawRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::toException);

        // 탈퇴 사유 저장
        MemberWithdrawal memberWithdrawal = MemberWithdrawal.builder()
                .type(request.type())
                .description(request.description())
                .build();
        memberWithdrawalRepository.save(memberWithdrawal);

        // 연동된 소셜 계정 정리
        List<SocialAccount> socialAccounts = socialAccountRepository.findByMember(member);
        socialAccountRepository.deleteAll(socialAccounts);

        // 회원 탈퇴 처리
        memberRepository.delete(member);

        // 로그아웃
        jwtService.logout(memberId);
    }
}
