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
import pyws.swyp.meeting.dto.MeetingRecordCreate;
import pyws.swyp.meeting.dto.MeetingRecordResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingRecord;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.MeetingType;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.RecordImage;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.MeetingRecordRepository;
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
class MeetingRecordServiceTest {

    @Autowired
    MeetingRecordService meetingRecordService;
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

    private Member member;
    private Meeting meetingDone;
    private Meeting meetingNotDone;

    @BeforeEach
    void setUp() {
        recordImageRepository.deleteAll();
        meetingRecordRepository.deleteAll();
        dateVoteRepository.deleteAll();
        timeVoteRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        meetingDone = Meeting.builder()
                .title("완료된 모임")
                .type(MeetingType.ACTIVE)
                .build();
        meetingDone.confirmDate(LocalDate.of(2026, 1, 1));
        meetingDone.confirmTime(LocalTime.of(18, 0));
        meetingDone.updateStatus(MeetingStatus.DONE);
        meetingRepository.save(meetingDone);

        meetingNotDone = Meeting.builder()
                .title("진행중 모임")
                .type(MeetingType.ACTIVE)
                .build();
        meetingNotDone.confirmDate(LocalDate.of(2026, 1, 2));
        meetingNotDone.confirmTime(LocalTime.of(19, 0));
        meetingRepository.save(meetingNotDone);

        member = memberRepository.save(Member.builder()
                .email("member@test.com")
                .nickname("member")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build());

        MeetingParticipant p1 = MeetingParticipant.builder()
                .meeting(meetingDone)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build();
        MeetingParticipant p2 = MeetingParticipant.builder()
                .meeting(meetingNotDone)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build();

        meetingParticipantRepository.saveAll(List.of(p1, p2));
    }

    @Test
    @DisplayName("모임이 DONE이면 리뷰를 생성하고 이미지 키가 있으면 ReviewImage도 저장된다.")
    void shouldCreateMeetingRecordReviewWithImages_whenMeetingDone() {
        // given
        Long memberId = member.getId();
        Long meetingId = meetingDone.getId();

        MeetingRecordCreate request = new MeetingRecordCreate(
                "좋았어요!",
                List.of("uploads/a.jpg", "uploads/b.jpg")
        );

        // when
        meetingRecordService.createMeetingRecord(memberId, meetingId, request);

        // then
        // 리뷰 1건
        MeetingRecord savedRecord = meetingRecordRepository
                .findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow();

        assertEquals("좋았어요!", savedRecord.getContent());

        // 이미지 2건
        List<RecordImage> images = recordImageRepository.findByMeetingRecordId(savedRecord.getId());
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

        MeetingRecordCreate req = new MeetingRecordCreate(
                "리뷰 작성",
                List.of("uploads/a.jpg")
        );

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingRecordService.createMeetingRecord(memberId, meetingId, req));

        // then
        assertEquals(ErrorCode.MEETING_NOT_DONE, ex.getErrorCode());
    }

    @Test
    @DisplayName("이미 리뷰가 존재하면 RECORD_ALREADY_EXISTS 예외가 발생한다.")
    void shouldThrowAlreadyExists_whenDuplicateReview() {
        // given
        Long memberId = member.getId();
        Long meetingId = meetingDone.getId();

        MeetingRecordCreate request = new MeetingRecordCreate(
                "첫 리뷰",
                List.of("uploads/a.jpg")
        );

        meetingRecordService.createMeetingRecord(memberId, meetingId, request);

        // when: 두 번째 생성 시도
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingRecordService.createMeetingRecord(memberId, meetingId, request));

        // then
        assertEquals(ErrorCode.RECORD_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 조회 시 이미지 키를 presigned URL로 변환해 응답에 포함한다")
    void shouldReturnReviewResponseWithPresignedImageUrls() {
        // given: 리뷰 + 이미지 저장
        MeetingRecord record = meetingRecordRepository.save(MeetingRecord.builder()
                .meeting(meetingDone)
                .member(member)
                .content("모임 후기")
                .build());

        recordImageRepository.saveAll(List.of(
                RecordImage.builder()
                        .meetingRecord(record)
                        .imageKey("uploads/x.jpg")
                        .sortOrder(0)
                        .build(),
                RecordImage.builder()
                        .meetingRecord(record)
                        .imageKey("uploads/y.jpg")
                        .sortOrder(1)
                        .build()
        ));

        // when
        MeetingRecordResponse response = meetingRecordService.getMeetingRecord(member.getId(), meetingDone.getId());

        // then
        assertEquals(LocalDate.of(2026, 1, 1), response.date());
        assertEquals(LocalTime.of(18, 0), response.time());
        assertEquals("모임 후기", response.content());

        assertEquals(2, response.imageUrls().size());
        assertEquals("https://test.local/presigned?key=uploads/x.jpg", response.imageUrls().get(0));
        assertEquals("https://test.local/presigned?key=uploads/y.jpg", response.imageUrls().get(1));
    }

    @Test
    @DisplayName("리뷰가 없으면 RECORD_NOT_FOUND 예외가 발생한다")
    void shouldThrowReviewNotFound_whenNoReview() {
        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> meetingRecordService.getMeetingRecord(member.getId(), meetingDone.getId()));

        // then
        assertEquals(ErrorCode.RECORD_NOT_FOUND, ex.getErrorCode());
    }
}