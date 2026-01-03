package pyws.swyp.meeting.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
import pyws.swyp.meeting.dto.MeetingReviewCreate;
import pyws.swyp.meeting.dto.MeetingReviewResponse;
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
class MeetingReviewServiceTest {

    @Autowired
    MeetingReviewService meetingReviewService;

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

    private Member member;
    private Meeting meetingDone;
    private Meeting meetingNotDone;
    private MeetingParticipant participantDone;
    private MeetingParticipant participantNotDone;

    @BeforeEach
    void setUp() {
        reviewImageRepository.deleteAll();
        meetingReviewRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        member = memberRepository.save(Member.builder()
                .email("member@test.com")
                .nickname("member")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build());

        meetingDone = Meeting.builder()
                .title("완료된 모임")
                .date(LocalDate.of(2026, 1, 1))
                .time(LocalTime.of(18, 0))
                .build();
        meetingDone.updateStatus(MeetingStatus.DONE);
        meetingRepository.save(meetingDone);

        meetingNotDone = Meeting.builder()
                .title("진행중 모임")
                .date(LocalDate.of(2026, 1, 2))
                .time(LocalTime.of(19, 0))
                .build();
        meetingNotDone.updateStatus(MeetingStatus.FIXED);
        meetingRepository.save(meetingNotDone);

        participantDone = meetingParticipantRepository.save(MeetingParticipant.builder()
                .meeting(meetingDone)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build());

        participantNotDone = meetingParticipantRepository.save(MeetingParticipant.builder()
                .meeting(meetingNotDone)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build());
    }

    @Test
    @DisplayName("모임이 DONE이면 리뷰를 생성하고 이미지 키가 있으면 ReviewImage도 저장된다.")
    void shouldCreateReviewWithImages_whenMeetingDone() {
        // given
        Long memberId = member.getId();
        Long meetingId = meetingDone.getId();

        MeetingReviewCreate req = new MeetingReviewCreate(
                "좋았어요!",
                List.of("uploads/a.jpg", "uploads/b.jpg")
        );

        // when
        meetingReviewService.create(memberId, meetingId, req);

        // then
        // 리뷰 1건
        MeetingReview savedReview = meetingReviewRepository
                .findByMeetingParticipantId(participantDone.getId())
                .orElseThrow();

        assertEquals("좋았어요!", savedReview.getContent());

        // 이미지 2건
        List<ReviewImage> images = reviewImageRepository.findAll();
        assertEquals(2, images.size());

        assertTrue(images.stream().anyMatch(i -> i.getImageKey().equals("uploads/a.jpg") && i.getSortOrder() == 0));
        assertTrue(images.stream().anyMatch(i -> i.getImageKey().equals("uploads/b.jpg") && i.getSortOrder() == 1));
    }

    @Test
    @DisplayName("모임이 DONE이 아니면 MEETING_NOT_DONE 예외가 발생한다.")
    void shouldThrowMeetingNotDone_whenMeetingNotDone() {
        // given
        Long memberId = member.getId();
        Long meetingId = meetingNotDone.getId();

        MeetingReviewCreate req = new MeetingReviewCreate(
                "리뷰 작성",
                List.of("uploads/a.jpg")
        );

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingReviewService.create(memberId, meetingId, req));

        // then
        assertEquals(ErrorCode.MEETING_NOT_DONE, ex.getErrorCode());
    }

    @Test
    @DisplayName("이미 리뷰가 존재하면 REVIEW_ALREADY_EXISTS 예외가 발생한다.")
    void shouldThrowAlreadyExists_whenDuplicateReview() {
        // given
        Long memberId = member.getId();
        Long meetingId = meetingDone.getId();

        MeetingReviewCreate req = new MeetingReviewCreate(
                "첫 리뷰",
                List.of("uploads/a.jpg")
        );

        meetingReviewService.create(memberId, meetingId, req);

        // when: 두 번째 생성 시도
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingReviewService.create(memberId, meetingId, req));

        // then
        assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 조회 시 이미지 키를 presigned URL로 변환해 응답에 포함한다")
    void shouldReturnReviewResponseWithPresignedImageUrls() {
        // given: 리뷰 + 이미지 저장
        MeetingReview savedReview = meetingReviewRepository.save(MeetingReview.builder()
                .meetingParticipant(participantDone)
                .content("모임 후기")
                .build());

        reviewImageRepository.saveAll(List.of(
                ReviewImage.builder()
                        .meetingReview(savedReview)
                        .imageKey("uploads/x.jpg")
                        .sortOrder(0)
                        .build(),
                ReviewImage.builder()
                        .meetingReview(savedReview)
                        .imageKey("uploads/y.jpg")
                        .sortOrder(1)
                        .build()
        ));

        // when
        MeetingReviewResponse response = meetingReviewService.getMeetingReview(member.getId(), meetingDone.getId());

        // then
        assertEquals(LocalDate.of(2026, 1, 1), response.date());
        assertEquals(LocalTime.of(18, 0), response.time());
        assertEquals("모임 후기", response.content());

        assertEquals(2, response.imageUrls().size());
        assertEquals("https://test.local/presigned?key=uploads/x.jpg", response.imageUrls().get(0));
        assertEquals("https://test.local/presigned?key=uploads/y.jpg", response.imageUrls().get(1));
    }

    @Test
    @DisplayName("리뷰가 없으면 REVIEW_NOT_FOUND 예외가 발생한다")
    void shouldThrowReviewNotFound_whenNoReview() {
        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingReviewService.getMeetingReview(member.getId(), meetingDone.getId()));

        // then
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, ex.getErrorCode());
    }
}