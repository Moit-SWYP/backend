package pyws.swyp.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pyws.swyp.config.TestPresignConfig;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MonthlyMeetingSummary;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingRecord;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.MeetingType;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.RecordImage;
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
@Import(TestPresignConfig.class)
class MeetingCalendarServiceTest {

    @Autowired
    MeetingCalendarService meetingCalendarService;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    MeetingRecordRepository meetingRecordRepository;
    @Autowired
    RecordImageRepository recordImageRepository;
    @Autowired
    DateVoteRepository dateVoteRepository;
    @Autowired
    TimeVoteRepository timeVoteRepository;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        dateVoteRepository.deleteAll();
        timeVoteRepository.deleteAll();
        recordImageRepository.deleteAll();
        meetingRecordRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        // meeting
        List<Meeting> meetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting meeting = Meeting.builder()
                    .title("테스트 모임 " + i)
                    .type(MeetingType.ACTIVE)
                    .build();
            meeting.updateStatus(MeetingStatus.FIXED);
            meeting.confirmDate(LocalDate.of(2026, 1, 1 + i));
            meeting.confirmTime(LocalTime.of(11 + i, 0));
            meetings.add(meeting);
        }
        meetingRepository.saveAll(meetings);

        // member
        member1 = Member.builder()
                .email("member1@test.com")
                .nickname("member1")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build();

        member2 = Member.builder()
                .email("member2@test.com")
                .nickname("member2")
                .birthDate(LocalDate.of(1999, 1, 2))
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build();

        memberRepository.saveAll(List.of(member1, member2));

        // memberParticipant
        List<MeetingParticipant> participants = new ArrayList<>();
        for (Meeting meeting : meetings) {
            participants.add(MeetingParticipant.builder()
                    .meeting(meeting)
                    .member(member1)
                    .role(ParticipantRole.HOST)
                    .build());

            participants.add(MeetingParticipant.builder()
                    .meeting(meeting)
                    .member(member2)
                    .role(ParticipantRole.MEMBER)
                    .build());
        }
        meetingParticipantRepository.saveAll(participants);

        // meetingRecord
        int reviewIndex = 1;
        List<MeetingRecord> records = new ArrayList<>();
        for (Meeting meeting : meetings) {
            records.add(MeetingRecord.builder()
                    .meeting(meeting)
                    .member(member1)
                    .content("모임 기록 " + reviewIndex++)
                    .build());
        }
        meetingRecordRepository.saveAll(records);

        // recordImage
        int imageIndex = 1;
        List<RecordImage> images = new ArrayList<>();
        for (MeetingRecord record : records) {
            images.add(RecordImage.builder()
                    .meetingRecord(record)
                    .imageKey("uploads/test-" + imageIndex++ + ".jpg")
                    .sortOrder(0)
                    .build());
        }
        recordImageRepository.saveAll(images);
    }

    @Test
    @DisplayName("해당 월 모임들을 날짜/시간 오름차순으로 조회하고 DTO로 변환한다.")
    void shouldReturnMonthlyMeetingsSortedAndMapped() {
        // given
        Long memberId = member1.getId(); // HOST 기준으로 검증
        int year = 2026;
        int month = 1;

        // when
        List<MonthlyMeetingSummary> result =
                meetingCalendarService.getMonthlyMeetings(memberId, year, month);

        // then
        assertEquals(5, result.size());  // 2026년 1월의 모임 개수

        // 정렬: 2026-01-01 11:00 -> 2026-01-05 15:00
        assertEquals(LocalDate.of(2026, 1, 1), result.getFirst().date());
        assertEquals(LocalTime.of(11, 0), result.getFirst().time());
        assertEquals(LocalDate.of(2026, 1, 5), result.getLast().date());
        assertEquals(LocalTime.of(15, 0), result.getLast().time());

        assertEquals("테스트 모임 0", result.getFirst().title());
        assertEquals(LocalDate.of(2026, 1, 1).getDayOfWeek(), result.getFirst().dayOfWeek());
    }

    @Test
    @DisplayName("기록 이미지 키가 있으면 Presigned GET URL이 생성되어 응답에 포함된다.")
    void shouldIncludePresignedImageUrlWhenReviewHasImageKey() {
        // given
        Long memberId = member1.getId();
        int year = 2026;
        int month = 1;

        // when
        List<MonthlyMeetingSummary> result =
                meetingCalendarService.getMonthlyMeetings(memberId, year, month);

        // then
        MonthlyMeetingSummary first = result.getFirst();

        assertFalse(first.recordImageUrls().isEmpty());
        assertTrue(first.recordImageUrls().getFirst().startsWith("https://test.local/presigned?key="));

        assertNotNull(first.recordContent());
        assertTrue(first.recordContent().startsWith("모임 기록 "));
    }

    @Test
    @DisplayName("해당 월에 모임이 없으면 빈 리스트가 반환된다.")
    void shouldThrowWhenNoMeetingsInMonth() {
        // given
        Long memberId = member1.getId();

        // when
        List<MonthlyMeetingSummary> response = meetingCalendarService.getMonthlyMeetings(memberId, 2026, 2);

        // then
        assertTrue(response.isEmpty());
    }
}