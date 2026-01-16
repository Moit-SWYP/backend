package pyws.swyp.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.MeetingType;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.vote.DateVote;
import pyws.swyp.meeting.entity.vote.TimeVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialAccountInfo;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.entity.MemberWithdrawal;
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
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    DateVoteRepository dateVoteRepository;
    @Autowired
    TimeVoteRepository timeVoteRepository;

    @MockitoBean
    JwtService jwtService;

    private Long memberId;
    private Member member;

    private Long meetingId;
    private Meeting meeting;

    private Long participantId;
    private MeetingParticipant participant;

    private List<SocialAccount> savedSocialAccounts;

    @BeforeEach
    void setUp() {
        dateVoteRepository.deleteAll();
        timeVoteRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        socialAccountRepository.deleteAll();
        memberWithdrawalRepository.deleteAll();
        memberRepository.deleteAll();

        this.member = memberRepository.save(Member.builder()
                .email("test@example.com")
                .nickname("테스트")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build());
        this.memberId = member.getId();

        List<SocialAccount> socialAccounts = List.of(
                SocialAccount.builder()
                        .member(member)
                        .socialProvider(SocialProvider.KAKAO)
                        .socialId("kakao-1")
                        .build(),
                SocialAccount.builder()
                        .member(member)
                        .socialProvider(SocialProvider.NAVER)
                        .socialId("naver-1")
                        .build()
        );

        socialAccountRepository.saveAll(socialAccounts);
        this.savedSocialAccounts = socialAccounts;

        meeting = Meeting.builder()
                .title("테스트 모임")
                .type(MeetingType.CULTURE_LOVER)
                .build();
        meeting.updateStatus(MeetingStatus.VOTING);
        meetingRepository.save(meeting);
        this.meetingId = meeting.getId();

        participant = MeetingParticipant.builder()
                .meeting(meeting)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build();
        meetingParticipantRepository.save(participant);
        this.participantId = participant.getId();

        dateVoteRepository.save(DateVote.builder()
                .meeting(meeting)
                .meetingParticipant(participant)
                .date(LocalDate.of(2025, 1, 1))
                .build());

        timeVoteRepository.save(TimeVote.builder()
                .meeting(meeting)
                .meetingParticipant(participant)
                .time(LocalTime.of(15, 30))
                .build());
    }

    @Test
    @DisplayName("로그인 정보를 조회한다.")
    void getMe_success() {
        // when
        MemberResponse response = memberService.getMe(member.getId());

        // then
        assertEquals("test@example.com", response.email());
        assertEquals("테스트", response.nickname());
        assertEquals(LocalDate.of(1999, 1, 1), response.birthDate());
        assertEquals(Gender.MALE, response.gender());
        assertEquals(MemberRole.MEMBER, response.memberRole());

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
    @DisplayName("회원 탈퇴 시 탈퇴사유 저장, 소셜 계정 삭제, 투표/참여자 정리, 회원 삭제, 로그아웃 호출")
    void withdraw_success_cascade() {
        // given
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.BUG, null);

        // when
        memberService.withdraw(memberId, request);

        // then
        Optional<Member> findMember = memberRepository.findById(memberId);
        assertTrue(findMember.isEmpty());

        List<MemberWithdrawal> withdrawals = memberWithdrawalRepository.findAll();
        assertEquals(1, withdrawals.size());
        assertEquals(WithdrawalType.BUG, withdrawals.getFirst().getType());

        assertEquals(0, socialAccountRepository.count());

        assertFalse(meetingParticipantRepository.existsById(participantId));
        assertEquals(0, dateVoteRepository.count());
        assertEquals(0, timeVoteRepository.count());

        verify(jwtService, times(1)).logout(memberId);
    }

    @Test
    @DisplayName("완료되지 않은 모임 HOST는 탈퇴 불가 + 로그아웃 미호출")
    void withdraw_denied_whenHostInUncompletedMeeting() {
        // given
        // BeforeEach로 생성한 participant role 변경
        ReflectionTestUtils.setField(participant, "role", ParticipantRole.HOST);
        meetingParticipantRepository.save(participant);

        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.ETC, "테스트");

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> memberService.withdraw(memberId, request));

        // then
        assertEquals(ErrorCode.HOST_CANNOT_WITHDRAW_WITH_UNCOMPLETED_MEETING, ex.getErrorCode());

        verify(jwtService, never()).logout(anyLong());
        assertTrue(memberRepository.existsById(memberId));
    }

    @Test
    @DisplayName("완료된 모임일 때는 HOST도 회원 탈퇴가 가능하다.")
    void withdraw_success_whenHostAndMeetingCompleted() {
        // given
        Meeting completed = Meeting.builder()
                .title("완료된 모임")
                .type(MeetingType.CULTURE_LOVER)
                .build();
        completed.updateStatus(MeetingStatus.DONE);
        meetingRepository.save(completed);

        MeetingParticipant host = MeetingParticipant.builder()
                .meeting(completed)
                .member(member)
                .role(ParticipantRole.HOST)
                .build();
        meetingParticipantRepository.save(host);

        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.ETC, "테스트");

        // when
        memberService.withdraw(memberId, request);

        // then
        Optional<Member> findMember = memberRepository.findById(memberId);
        assertTrue(findMember.isEmpty());

        List<MemberWithdrawal> withdrawals = memberWithdrawalRepository.findAll();
        assertEquals(1, withdrawals.size());
        assertEquals(WithdrawalType.ETC, withdrawals.getFirst().getType());

        assertEquals(0, socialAccountRepository.count());

        assertFalse(meetingParticipantRepository.existsById(participantId));
        assertEquals(0, dateVoteRepository.count());
        assertEquals(0, timeVoteRepository.count());

        verify(jwtService, times(1)).logout(memberId);
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

    @Test
    @DisplayName("프로필 캐릭터를 변경한다.")
    void updateCharacter_success() {
        //given
        CharacterType characterType = CharacterType.CULTURE_LOVER;

        //when
        memberService.updateCharacter(member.getId(), characterType);

        //then
        Member member = memberRepository.findById(this.member.getId()).get();
        assertEquals(characterType, member.getCharacterType());
    }
}
