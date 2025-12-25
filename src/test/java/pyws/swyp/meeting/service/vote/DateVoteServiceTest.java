package pyws.swyp.meeting.service.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pyws.swyp.meeting.dto.vote.DateVoteRequest;
import pyws.swyp.meeting.dto.vote.DateVoterResponse;
import pyws.swyp.meeting.dto.vote.DateVotersResponse;
import pyws.swyp.meeting.dto.vote.VotedDatesResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.vote.DateOption;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateOptionRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.repository.MemberRepository;

@SpringBootTest
class DateVoteServiceTest {

    @Autowired
    DateVoteService dateVoteService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    DateOptionRepository dateOptionRepository;
    @Autowired
    DateVoteRepository dateVoteRepository;

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

        dateVoteRepository.deleteAll();
        dateOptionRepository.deleteAll();
    }

    @Test
    @DisplayName("첫 날짜 투표에 성공한다.")
    void voteDates_createsOptionsAndVotes() {
        // given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 12);

        DateVoteRequest request = new DateVoteRequest(List.of(d1, d2));
        Long memberId = p1.getMember().getId();

        // when
        dateVoteService.voteDates(memberId, meeting.getId(), request);

        // then
        List<DateOption> options = dateOptionRepository.findAll();
        assertEquals(2, options.size());

        List<LocalDate> dates = options.stream()
                .map(DateOption::getDate)
                .toList();

        assertTrue(dates.contains(d1));
        assertTrue(dates.contains(d2));

        List<Long> optionIds = dateVoteRepository.findOptionIdsByMeetingParticipantId(p1.getId());
        assertEquals(2, optionIds.size());
    }

    @Test
    @DisplayName("재투표 시에 기존 투표 기록은 덮어써진다.")
    void voteDates_overwriteReplacesVotes() {
        // given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 12);
        LocalDate d3 = LocalDate.of(2025, 1, 11);

        Long memberId = p1.getMember().getId();

        DateVoteRequest firstRequest = new DateVoteRequest(List.of(d1, d2));
        DateVoteRequest secondRequest = new DateVoteRequest(List.of(d3));

        // when
        dateVoteService.voteDates(memberId, meeting.getId(), firstRequest);
        dateVoteService.voteDates(memberId, meeting.getId(), secondRequest);

        // then
        List<Long> optionIds = dateVoteRepository.findOptionIdsByMeetingParticipantId(p1.getId());
        assertEquals(1, optionIds.size());

        DateOption optD3 = dateOptionRepository.findAll().stream()
                .filter(o -> o.getDate().equals(d3))
                .findFirst()
                .orElseThrow();

        assertEquals(optD3.getId(), optionIds.get(0));
    }

    @Test
    @DisplayName("0표가 된 투표 후보는 제거된다.")
    void voteDates_cleanupOrphanOptions_deletedWhenNoVotes() {
        // given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 12);
        LocalDate d3 = LocalDate.of(2025, 1, 11);

        Long memberId = p1.getMember().getId();

        DateVoteRequest firstRequest = new DateVoteRequest(List.of(d1, d2));
        DateVoteRequest secondRequest = new DateVoteRequest(List.of(d1, d3));

        // when
        dateVoteService.voteDates(memberId, meeting.getId(), firstRequest);
        dateVoteService.voteDates(memberId, meeting.getId(), secondRequest);

        // then
        List<LocalDate> remainingDates = dateOptionRepository.findAll().stream()
                .map(DateOption::getDate)
                .toList();

        assertEquals(secondRequest.dates().size(), remainingDates.size());
        assertTrue(remainingDates.contains(d1));
        assertTrue(remainingDates.contains(d3));
        assertFalse(remainingDates.contains(d2));
    }

    @Test
    @DisplayName("다른 사람이 사용 중인 DateOption은 orphan 정리로 삭제되지 않는다")
    void voteDates_cleanupDoesNotDeleteOptionsUsedByOthers() {
        // given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 12);
        LocalDate d3 = LocalDate.of(2025, 1, 11);

        Long memberId = p1.getMember().getId();
        Long anotherId = p2.getMember().getId();
        Long meetingId = meeting.getId();

        // when
        dateVoteService.voteDates(memberId, meetingId, new DateVoteRequest(List.of(d1, d2)));
        dateVoteService.voteDates(anotherId, meetingId, new DateVoteRequest(List.of(d2)));
        dateVoteService.voteDates(memberId, meetingId, new DateVoteRequest(List.of(d1, d3)));

        // then
        Set<LocalDate> remainingDates = dateOptionRepository.findAll().stream()
                .map(DateOption::getDate)
                .collect(Collectors.toSet());

        assertTrue(remainingDates.contains(d2));
    }

    @Test
    @DisplayName("날짜 투표 Top N은 투표 수 내림차순, 날짜 오름차순으로 정렬된다.")
    void getTopDateOptions_returnsRankedLocalDates() {
        // given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 11);
        LocalDate d3 = LocalDate.of(2025, 1, 12);
        LocalDate d4 = LocalDate.of(2025, 1, 13);

        Long meetingId = meeting.getId();
        Long memberId1 = p1.getMember().getId();
        Long memberId2 = p2.getMember().getId();
        Long memberId3 = p3.getMember().getId();

        // d1: 2, d2: 3, d3: 2, d4: 1
        dateVoteService.voteDates(memberId1, meetingId, new DateVoteRequest(List.of(d1, d2)));
        dateVoteService.voteDates(memberId2, meetingId, new DateVoteRequest(List.of(d2, d3)));
        dateVoteService.voteDates(memberId3, meetingId, new DateVoteRequest(List.of(d1, d2, d3, d4)));

        int limit = 2;

        // when
        VotedDatesResponse response = dateVoteService.getTopDateOptions(memberId1, meetingId, limit);
        List<LocalDate> topDates = response.dates();

        // then
        assertEquals(limit, topDates.size());
        assertEquals(List.of(d1, d2), topDates);
    }

    @Test
    @DisplayName("투표된 날짜 목록을 조회한다.")
    void getVotedDates_returnsVotedDates() {
        // given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 11);
        LocalDate d3 = LocalDate.of(2025, 1, 12);
        LocalDate d4 = LocalDate.of(2025, 1, 13);

        Long meetingId = meeting.getId();
        Long memberId1 = p1.getMember().getId();
        Long memberId2 = p2.getMember().getId();
        Long memberId3 = p3.getMember().getId();

        dateVoteService.voteDates(memberId1, meetingId, new DateVoteRequest(List.of(d1, d2)));
        dateVoteService.voteDates(memberId2, meetingId, new DateVoteRequest(List.of(d2, d3)));
        dateVoteService.voteDates(memberId3, meetingId, new DateVoteRequest(List.of(d1, d2, d3, d4)));

        // when
        VotedDatesResponse response = dateVoteService.getVotedDates(memberId1, meetingId);

        // then
        assertEquals(List.of(d1, d2, d3, d4), response.dates());
    }

    @Test
    @DisplayName("특정 날짜에 투표한 모임원을 조회한다.")
    void getVotersByDate_returnsVoters() {
        //given
        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 1, 11);
        LocalDate d3 = LocalDate.of(2025, 1, 12);
        LocalDate d4 = LocalDate.of(2025, 1, 13);

        Long meetingId = meeting.getId();
        Long memberId1 = p1.getMember().getId();
        Long memberId2 = p2.getMember().getId();
        Long memberId3 = p3.getMember().getId();

        // d1 -> p1, p3
        dateVoteService.voteDates(memberId1, meetingId, new DateVoteRequest(List.of(d1, d2)));
        dateVoteService.voteDates(memberId2, meetingId, new DateVoteRequest(List.of(d2, d3)));
        dateVoteService.voteDates(memberId3, meetingId, new DateVoteRequest(List.of(d1, d2, d3, d4)));

        //when: d1에 투표한 모임원 조회
        DateVotersResponse response = dateVoteService.getVotersByDate(memberId1, meetingId, d1);

        //then
        assertEquals(2, response.voters().size());

        List<Long> voterIds = response.voters().stream()
                .map(DateVoterResponse::memberId)
                .toList();

        assertTrue(voterIds.contains(memberId1));
        assertTrue(voterIds.contains(memberId3));
    }
}