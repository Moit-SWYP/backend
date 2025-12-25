package pyws.swyp.meeting.service.vote;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.vote.TimeVoteRequest;
import pyws.swyp.meeting.dto.vote.TimeVoterResponse;
import pyws.swyp.meeting.dto.vote.TimeVotersResponse;
import pyws.swyp.meeting.dto.vote.TopVotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VotedTimesResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.vote.TimeOption;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.TimeOptionRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.repository.MemberRepository;

@SpringBootTest
class TimeVoteServiceTest {

    @Autowired
    TimeVoteService timeVoteService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    TimeOptionRepository timeOptionRepository;
    @Autowired
    TimeVoteRepository timeVoteRepository;

    private Meeting meeting;
    private MeetingParticipant p1;
    private MeetingParticipant p2;
    private MeetingParticipant p3;

    @BeforeEach
    void setUp() {
        Meeting meeting = Meeting.builder()
                .title("테스트 모임")
                .build();
        meeting.updateStatus(MeetingStatus.DATE_VOTING);
        this.meeting = meetingRepository.save(meeting);

        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Member member = Member.builder()
                    .email("test" + i + "@test.com")
                    .nickname("test" + i)
                    .birthDate(LocalDate.now())
                    .gender(Gender.MALE)
                    .role(MemberRole.MEMBER)
                    .characterType(CharacterType.ACTIVE)
                    .build();
            members.add(memberRepository.save(member));
        }

        List<MeetingParticipant> participants = new ArrayList<>();
        for (Member member : members) {
            MeetingParticipant participant = MeetingParticipant.builder()
                    .meeting(meeting)
                    .member(member)
                    .role(ParticipantRole.MEMBER)
                    .build();
            participants.add(meetingParticipantRepository.save(participant));
        }

        p1 = participants.get(0);
        p2 = participants.get(1);
        p3 = participants.get(2);

        timeVoteRepository.deleteAll();
        timeOptionRepository.deleteAll();
    }

    @Test
    @DisplayName("첫 시간 투표에 성공한다.")
    void voteTimes_createsOptionsAndVotes() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);

        TimeVoteRequest request = new TimeVoteRequest(List.of(t1, t2));
        Long memberId = p1.getMember().getId();

        // when
        timeVoteService.voteTimes(memberId, meeting.getId(), request);

        // then
        List<TimeOption> options = timeOptionRepository.findAll();
        assertEquals(2, options.size());

        List<LocalTime> times = options.stream()
                .map(TimeOption::getTime)
                .toList();

        assertTrue(times.contains(t1));
        assertTrue(times.contains(t2));

        List<Long> optionIds = timeVoteRepository.findOptionIdsByMeetingParticipantId(p1.getId());
        assertEquals(2, optionIds.size());
    }

    @Test
    @DisplayName("재투표 시에 기존 투표 기록은 덮어써진다.")
    void voteTimes_overwriteReplacesVotes() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        Long memberId = p1.getMember().getId();

        TimeVoteRequest firstRequest = new TimeVoteRequest(List.of(t1, t2));
        TimeVoteRequest secondRequest = new TimeVoteRequest(List.of(t3));

        // when
        timeVoteService.voteTimes(memberId, meeting.getId(), firstRequest);
        timeVoteService.voteTimes(memberId, meeting.getId(), secondRequest);

        // then
        List<Long> optionIds = timeVoteRepository.findOptionIdsByMeetingParticipantId(p1.getId());
        assertEquals(1, optionIds.size());

        TimeOption optT3 = timeOptionRepository.findAll().stream()
                .filter(o -> o.getTime().equals(t3))
                .findFirst()
                .orElseThrow();

        assertEquals(optT3.getId(), optionIds.getFirst());
    }

    @Test
    @DisplayName("0표가 된 투표 후보는 제거된다.")
    void voteTimes_cleanupOrphanOptions_deletedWhenNoVotes() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        Long memberId = p1.getMember().getId();

        TimeVoteRequest firstRequest = new TimeVoteRequest(List.of(t1, t2));
        TimeVoteRequest secondRequest = new TimeVoteRequest(List.of(t1, t3));

        // when
        timeVoteService.voteTimes(memberId, meeting.getId(), firstRequest);
        timeVoteService.voteTimes(memberId, meeting.getId(), secondRequest);

        // then
        List<LocalTime> remainingTimes = timeOptionRepository.findAll().stream()
                .map(TimeOption::getTime)
                .toList();

        assertEquals(secondRequest.times().size(), remainingTimes.size());
        assertTrue(remainingTimes.contains(t1));
        assertTrue(remainingTimes.contains(t3));
        assertFalse(remainingTimes.contains(t2));
    }

    @Test
    @DisplayName("다른 사람이 사용 중인 TimeOption은 orphan 정리로 삭제되지 않는다")
    void voteTimes_cleanupDoesNotDeleteOptionsUsedByOthers() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        Long memberId = p1.getMember().getId();
        Long anotherId = p2.getMember().getId();
        Long meetingId = meeting.getId();

        // when
        timeVoteService.voteTimes(memberId, meetingId, new TimeVoteRequest(List.of(t1, t2)));
        timeVoteService.voteTimes(anotherId, meetingId, new TimeVoteRequest(List.of(t2)));
        timeVoteService.voteTimes(memberId, meetingId, new TimeVoteRequest(List.of(t1, t3)));

        // then
        Set<LocalTime> remainingTimes = timeOptionRepository.findAll().stream()
                .map(TimeOption::getTime)
                .collect(Collectors.toSet());

        assertTrue(remainingTimes.contains(t2));
    }

    @Test
    @DisplayName("최다 득표 시간 1개를 조회한다. (동점이면 시간 오름차순)")
    void getTopVotedTime_returnsTop1() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        Long meetingId = meeting.getId();
        Long memberId1 = p1.getMember().getId();
        Long memberId2 = p2.getMember().getId();
        Long memberId3 = p3.getMember().getId();

        // 득표수:
        // t1: 2 (p1, p3)
        // t2: 2 (p2, p3)  -> 동점, 더 이른 시간이 top1이어야 함 (t1)
        // t3: 1 (p1)
        timeVoteService.voteTimes(memberId1, meetingId, new TimeVoteRequest(List.of(t1, t3)));
        timeVoteService.voteTimes(memberId2, meetingId, new TimeVoteRequest(List.of(t2)));
        timeVoteService.voteTimes(memberId3, meetingId, new TimeVoteRequest(List.of(t1, t2)));

        // when
        TopVotedTimeResponse response = timeVoteService.getTopVotedTime(memberId1, meetingId);

        // then
        assertEquals(t1, response.time());
    }

    @Test
    @DisplayName("투표된 시간들과 각 투표수를 조회한다.")
    void getVotedTimesWithCounts_returnsTimesAndCounts() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        Long meetingId = meeting.getId();
        Long memberId1 = p1.getMember().getId();
        Long memberId2 = p2.getMember().getId();
        Long memberId3 = p3.getMember().getId();

        // t1:2, t2:3, t3:1
        timeVoteService.voteTimes(memberId1, meetingId, new TimeVoteRequest(List.of(t1, t2)));
        timeVoteService.voteTimes(memberId2, meetingId, new TimeVoteRequest(List.of(t2)));
        timeVoteService.voteTimes(memberId3, meetingId, new TimeVoteRequest(List.of(t1, t2, t3)));

        // when
        VotedTimesResponse response = timeVoteService.getVotedTimesWithCounts(memberId1, meetingId);

        // then
        Map<LocalTime, Long> countMap = response.times().stream()
                .collect(Collectors.toMap(VotedTimeResponse::time, VotedTimeResponse::count));

        assertEquals(2L, countMap.get(t1));
        assertEquals(3L, countMap.get(t2));
        assertEquals(1L, countMap.get(t3));
    }

    @Test
    @DisplayName("특정 시간에 투표한 모임원을 조회한다.")
    void getVotersByTime_returnsVoters() {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        Long meetingId = meeting.getId();
        Long memberId1 = p1.getMember().getId();
        Long memberId2 = p2.getMember().getId();
        Long memberId3 = p3.getMember().getId();

        // t1 -> p1, p3
        timeVoteService.voteTimes(memberId1, meetingId, new TimeVoteRequest(List.of(t1, t2)));
        timeVoteService.voteTimes(memberId2, meetingId, new TimeVoteRequest(List.of(t2, t3)));
        timeVoteService.voteTimes(memberId3, meetingId, new TimeVoteRequest(List.of(t1, t2, t3)));

        // when
        TimeVotersResponse response = timeVoteService.getVotersByTime(memberId1, meetingId, t1);

        // then
        assertEquals(2, response.voters().size());

        List<Long> voterIds = response.voters().stream()
                .map(TimeVoterResponse::memberId)
                .toList();

        assertTrue(voterIds.contains(memberId1));
        assertTrue(voterIds.contains(memberId3));
    }

    @Test
    @DisplayName("시간은 30분 단위가 아니면 예외가 발생한다.")
    void voteTimes_invalidUnit_throwsException() {
        // given
        LocalTime invalid = LocalTime.of(15, 10);
        Long memberId = p1.getMember().getId();

        // expected
        CustomException ex = assertThrows(CustomException.class,
                () -> timeVoteService.voteTimes(memberId, meeting.getId(), new TimeVoteRequest(List.of(invalid))));

        assertEquals(ErrorCode.TIME_NOT_IN_30_MIN_UNIT, ex.getErrorCode());
    }
}
