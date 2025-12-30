package pyws.swyp.member.service;

import static pyws.swyp.global.error.ErrorCode.HOST_CANNOT_WITHDRAW_WITH_UNCOMPLETED_MEETING;
import static pyws.swyp.global.error.ErrorCode.MEMBER_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialAccountInfo;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberWithdrawal;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.MemberWithdrawalRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MemberWithdrawCleanupService memberWithdrawCleanupService;
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
                member.getCharacterType(),
                socialAccounts
        );
    }

    @Transactional
    public void withdraw(Long memberId, MemberWithdrawRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::toException);

        // 완료되지 않은 모임의 HOST는 탈퇴 불가
        if (meetingParticipantRepository.existsHostInUncompletedMeetings(memberId)) {
            throw HOST_CANNOT_WITHDRAW_WITH_UNCOMPLETED_MEETING.toException();
        }

        // 탈퇴 사유 저장
        MemberWithdrawal memberWithdrawal = MemberWithdrawal.builder()
                .type(request.type())
                .description(request.description())
                .build();
        memberWithdrawalRepository.save(memberWithdrawal);

        // 연동된 소셜 계정 정리
        socialAccountRepository.deleteAllByMemberId(memberId);

        // 연관된 자식 제거
        memberWithdrawCleanupService.cleanup(memberId);

        // 로그아웃
        jwtService.logout(memberId);

        // 회원 삭제
        memberRepository.delete(member);
    }

    @Transactional
    public void updateCharacter(Long memberId, CharacterType character) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::toException);
        member.updateCharacterType(character);
    }
}
