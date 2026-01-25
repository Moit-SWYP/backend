package pyws.swyp.meeting.service.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pyws.swyp.meeting.dto.vote.VoteSummary;
import pyws.swyp.meeting.dto.vote.date.DateSummary;
import pyws.swyp.meeting.dto.vote.time.TimeSummary;
import pyws.swyp.meeting.dto.vote.time.VotedTimeResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.MeetingType;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.vote.DateVote;
import pyws.swyp.meeting.entity.vote.TimeVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRecordRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.RecordImageRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.repository.MemberRepository;

@SpringBootTest
class VoteSummaryServiceTest {

    @Autowired
    VoteSummaryService voteSummaryService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    DateVoteRepository dateVoteRepository;
    @Autowired
    TimeVoteRepository timeVoteRepository;
    @Autowired
    MeetingRecordRepository meetingRecordRepository;
    @Autowired
    RecordImageRepository recordImageRepository;

    private Meeting meeting;
    private MeetingParticipant p1, p2, p3;
    private LocalDate d1, d2, d3, d4;
    private LocalTime t1, t2, t3, t4;

    @BeforeEach
    void setUp() {
        recordImageRepository.deleteAll();
        meetingRecordRepository.deleteAll();
        timeVoteRepository.deleteAll();
        dateVoteRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        // meeting
        this.meeting = meetingRepository.save(Meeting.builder()
                .title("테스트 모임")
                .type(MeetingType.DRINKER)
                .build());

        // member
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

        // memberParticipant
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

        // dateVote
        // d1: p1, p3
        // d2: p1, p2
        // d3: p2
        // d4: p3
        d1 = LocalDate.of(2025, 1, 10);
        d2 = LocalDate.of(2025, 1, 11);
        d3 = LocalDate.of(2025, 1, 12);
        d4 = LocalDate.of(2025, 1, 13);

        dateVoteRepository.saveAll(List.of(
                DateVote.builder().meeting(meeting).meetingParticipant(p1).date(d1).build(),
                DateVote.builder().meeting(meeting).meetingParticipant(p3).date(d1).build(),

                DateVote.builder().meeting(meeting).meetingParticipant(p1).date(d2).build(),
                DateVote.builder().meeting(meeting).meetingParticipant(p2).date(d2).build(),

                DateVote.builder().meeting(meeting).meetingParticipant(p2).date(d3).build(),
                DateVote.builder().meeting(meeting).meetingParticipant(p3).date(d4).build()
        ));

        // timeVote
        // t1: p1, p2
        // t2: p1, p3
        // t3: p2
        // t4: p3
        t1 = LocalTime.of(15, 0);
        t2 = LocalTime.of(15, 30);
        t3 = LocalTime.of(16, 0);
        t4 = LocalTime.of(16, 30);

        timeVoteRepository.saveAll(List.of(
                TimeVote.builder().meeting(meeting).meetingParticipant(p1).time(t1).build(),
                TimeVote.builder().meeting(meeting).meetingParticipant(p2).time(t1).build(),

                TimeVote.builder().meeting(meeting).meetingParticipant(p1).time(t2).build(),
                TimeVote.builder().meeting(meeting).meetingParticipant(p3).time(t2).build(),

                TimeVote.builder().meeting(meeting).meetingParticipant(p2).time(t3).build(),
                TimeVote.builder().meeting(meeting).meetingParticipant(p3).time(t4).build()
        ));
    }

    @Test
    @DisplayName("VoteSummary가 정상 생성된다.")
    void getVoteSummary_success() {
        // when
        VoteSummary response = voteSummaryService.getVoteSummary(
                p1.getMember().getId(),
                meeting.getId()
        );

        // then
        assertFalse(response.isHost());

        DateSummary dateSummary = response.dateSummary();
        assertNotNull(dateSummary);
        assertEquals(List.of(d1, d2), dateSummary.topDates());
        assertEquals(List.of(d1, d2, d3, d4), dateSummary.votedDates());

        TimeSummary timeSummary = response.timeSummary();
        assertNotNull(timeSummary);
        assertEquals(List.of(t1, t2), timeSummary.topTimes());

        List<VotedTimeResponse> expected = List.of(
                new VotedTimeResponse(t1, 2),
                new VotedTimeResponse(t2, 2),
                new VotedTimeResponse(t3, 1),
                new VotedTimeResponse(t4, 1)
        );
        assertEquals(expected, timeSummary.votedTimes());

        List<LocalTime> myVotedTimes = timeSummary.myVotedTimes();
        assertEquals(List.of(t1, t2), myVotedTimes);
    }
}