package pyws.swyp.meeting.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.entity.*;
import pyws.swyp.meeting.entity.vote.DateVote;
import pyws.swyp.meeting.entity.vote.TimeVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.repository.MemberRepository;

@SpringBootTest
class MeetingConfirmServiceTest {

    @Autowired
    MeetingConfirmService meetingConfirmService;

    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    DateVoteRepository dateVoteRepository;
    @Autowired
    TimeVoteRepository timeVoteRepository;
    @Autowired
    MemberRepository memberRepository;

    private Long hostMemberId;
    private Long normalMemberId;
    private Long meetingId;
    private Meeting meeting;

    @BeforeEach
    void setUp() {
        dateVoteRepository.deleteAll();
        timeVoteRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        Meeting meeting = Meeting.builder()
                .title("테스트 모임")
                .type(MeetingType.DRINKER)
                .build();
        meeting.updateStatus(MeetingStatus.DATE_VOTING);
        meetingRepository.save(meeting);
        this.meetingId = meeting.getId();
        this.meeting = meeting;

        Member host = Member.builder()
                .email("host@test.com")
                .nickname("host")
                .birthDate(LocalDate.now())
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build();
        Member member = Member.builder()
                .email("member@test.com")
                .nickname("member")
                .birthDate(LocalDate.now())
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build();

        memberRepository.save(host);
        memberRepository.save(member);

        this.hostMemberId = host.getId();
        this.normalMemberId = member.getId();

        MeetingParticipant hostParticipant = MeetingParticipant.builder()
                .meeting(meeting)
                .member(host)
                .role(ParticipantRole.HOST)
                .build();

        MeetingParticipant participant = MeetingParticipant.builder()
                .meeting(meeting)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build();

        meetingParticipantRepository.save(hostParticipant);
        meetingParticipantRepository.save(participant);

        DateVote dateVote = DateVote.builder()
                .meeting(meeting)
                .meetingParticipant(hostParticipant)
                .date(LocalDate.of(2025, 1, 1))
                .build();
        dateVoteRepository.save(dateVote);

        TimeVote timevote = TimeVote.builder()
                .meeting(meeting)
                .meetingParticipant(participant)
                .time(LocalTime.of(15, 30))
                .build();
        timeVoteRepository.save(timevote);
    }

    @Test
    @DisplayName("모임장이 최다 득표 날짜로 확정하면 상태가 DATE_VOTED가 된다.")
    void confirmDateVote_success() {
        // when
        meetingConfirmService.confirmDateVote(hostMemberId, meetingId);

        // then
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.DATE_VOTED, meeting.getStatus());
        assertEquals(LocalDate.of(2025, 1, 1), meeting.getDate());
    }

    @Test
    @DisplayName("모임장이 지정한 날짜로 확정하면 상태가 DATE_VOTED가 된다.")
    void confirmDate_manual_success() {
        // given
        LocalDate chosen = LocalDate.of(2025, 2, 2);

        // when
        meetingConfirmService.confirmDate(hostMemberId, meetingId, chosen);

        // then
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.DATE_VOTED, meeting.getStatus());
        assertEquals(chosen, meeting.getDate());
    }

    @Test
    @DisplayName("모임장이 모임 날짜 확정을 취소하면 상태가 DATE_VOTING으로 바뀌고 확정 날짜가 null이 된다.")
    void cancelConfirmDateVote_success() {
        // given: 먼저 확정 상태 만들기
        meetingConfirmService.confirmDateVote(hostMemberId, meetingId);

        // when
        meetingConfirmService.cancelConfirmDateVote(hostMemberId, meetingId);

        // then
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.DATE_VOTING, meeting.getStatus());
        assertNull(meeting.getDate());
    }

    @Test
    @DisplayName("모임장이 최다 득표 시간으로 확정하면 상태가 TIME_VOTED가 된다.")
    void confirmTimeVote_success() {
        // given
        meeting.updateStatus(MeetingStatus.TIME_VOTING);
        meetingRepository.save(meeting);

        // when
        meetingConfirmService.confirmTimeVote(hostMemberId, meetingId);

        // then
        Meeting updated = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.TIME_VOTED, updated.getStatus());
        assertEquals(LocalTime.of(15, 30), updated.getTime());
    }

    @Test
    @DisplayName("모임장이 지정한 시간으로 확정하면 상태가 TIME_VOTED가 된다.")
    void confirmTime_manual_success() {
        // given
        meeting.updateStatus(MeetingStatus.TIME_VOTING);
        meetingRepository.save(meeting);

        LocalTime chosen = LocalTime.of(20, 0);

        // when
        meetingConfirmService.confirmTime(hostMemberId, meetingId, chosen);

        // then
        Meeting updated = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.TIME_VOTED, updated.getStatus());
        assertEquals(chosen, updated.getTime());
    }

    @Test
    @DisplayName("모임장이 모임 시간 확정을 취소하면 TIME_VOTING으로 바뀌고 확정 시간은 null이 된다.")
    void cancelConfirmTimeVote_success() {
        // given
        meeting.updateStatus(MeetingStatus.TIME_VOTING);
        meetingRepository.save(meeting);

        meetingConfirmService.confirmTime(hostMemberId, meetingId, LocalTime.of(20, 0));

        // when
        meetingConfirmService.cancelConfirmTimeVote(hostMemberId, meetingId);

        // then
        Meeting updated = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.TIME_VOTING, updated.getStatus());
        assertNull(updated.getTime());
    }

    @Test
    @DisplayName("모임장이 아니면 MEETING_HOST_ONLY 예외가 발생한다.")
    void notHost_throw() {
        // expected
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingConfirmService.confirmDateVote(normalMemberId, meetingId));

        assertEquals(ErrorCode.MEETING_HOST_ONLY, ex.getErrorCode());
    }

    @Test
    @DisplayName("모임이 투표 중 상태가 아니면 MEETING_NOT_CONFIRMABLE 예외가 발생한다.")
    void confirmDateVote_wrongStatus_throw() {
        // given
        meeting.updateStatus(MeetingStatus.CREATED);
        meetingRepository.save(meeting);

        // expected
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingConfirmService.confirmDateVote(hostMemberId, meetingId));

        assertEquals(ErrorCode.MEETING_NOT_CONFIRMABLE, ex.getErrorCode());
    }
}