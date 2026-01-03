package pyws.swyp.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MonthlyMeetingSummary;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingReview;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.ReviewImage;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.MeetingReviewRepository;
import pyws.swyp.meeting.repository.ReviewImageRepository;
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
    MeetingReviewRepository meetingReviewRepository;
    @Autowired
    ReviewImageRepository reviewImageRepository;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        reviewImageRepository.deleteAll();
        meetingReviewRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        List<Meeting> meetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting meeting = Meeting.builder()
                    .title("테스트 모임 " + i)
                    .date(LocalDate.of(2026, 1, 1 + i))
                    .time(LocalTime.of(11 + i, 0))
                    .build();
            meeting.updateStatus(MeetingStatus.FIXED);
            meetings.add(meeting);
        }
        meetingRepository.saveAll(meetings);

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

        List<MeetingParticipant> participants = new ArrayList<>();
        for (Meeting meeting : meetingRepository.findAll()) {
            MeetingParticipant host = MeetingParticipant.builder()
                    .meeting(meeting)
                    .member(member1)
                    .role(ParticipantRole.HOST)
                    .build();

            MeetingParticipant participant = MeetingParticipant.builder()
                    .meeting(meeting)
                    .member(member2)
                    .role(ParticipantRole.MEMBER)
                    .build();

            participants.add(host);
            participants.add(participant);
        }
        meetingParticipantRepository.saveAll(participants);

        int reviewIndex = 1;
        List<MeetingReview> reviews = new ArrayList<>();
        for (MeetingParticipant participant : meetingParticipantRepository.findAll()) {
            MeetingReview meetingReview = MeetingReview.builder()
                    .meetingParticipant(participant)
                    .content("모임 리뷰 " + reviewIndex++)
                    .build();
            reviews.add(meetingReview);
        }
        meetingReviewRepository.saveAll(reviews);

        // 5) 모든 리뷰에 대해 이미지 1장씩 생성/저장
        int imageIndex = 1;
        List<ReviewImage> images = new ArrayList<>();
        for (MeetingReview meetingReview : meetingReviewRepository.findAll()) {
            ReviewImage image = ReviewImage.builder()
                    .meetingReview(meetingReview)
                    .imageKey("uploads/test-" + imageIndex++ + ".jpg")
                    .sortOrder(1)
                    .build();

            images.add(image);
        }
        reviewImageRepository.saveAll(images);
    }

    @Test
    @DisplayName("해당 월 모임들을 날짜/시간 오름차순으로 조회하고 DTO로 변환한다")
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
        assertEquals(LocalDate.of(2026, 1, 1).getDayOfWeek().name(), result.getFirst().dayOfWeek());

        assertTrue(result.getFirst().isDateVoteDone());
        assertTrue(result.getFirst().isTimeVoteDone());
    }

    @Test
    @DisplayName("리뷰 이미지 키가 있으면 Presigned GET URL이 생성되어 응답에 포함된다.")
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

        assertTrue(first.hasReview());

        assertNotNull(first.reviewImageUrl());
        assertTrue(first.reviewImageUrl().startsWith("https://test.local/presigned?key="));

        assertNotNull(first.reviewContent());
        assertTrue(first.reviewContent().startsWith("모임 리뷰 "));
    }

    @Test
    @DisplayName("해당 월에 모임이 없으면 MEETING_NOT_FOUND_IN_MONTH 예외가 발생한다")
    void shouldThrowWhenNoMeetingsInMonth() {
        // given
        Long memberId = member1.getId();

        // expected
        CustomException ex = assertThrows(CustomException.class, () ->
                meetingCalendarService.getMonthlyMeetings(memberId, 2026, 2)
        );

        assertEquals(ErrorCode.MEETING_NOT_FOUND_IN_MONTH, ex.getErrorCode());
    }
}