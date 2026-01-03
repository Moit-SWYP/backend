package pyws.swyp.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingBriefResponse;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MeetingService meetingService;

    LocalDate nowDate = LocalDate.now();
    LocalDateTime nowDateTime = LocalDateTime.now();

    /*
    * 모임 생성
    */
    @Test
    @DisplayName("모임 생성에 성공한다")
    void 모임_생성_성공() {
        // given
        Long memberId = 1L;
        MeetingCreateRequest request = mock(MeetingCreateRequest.class);
        Meeting meeting = Meeting.builder()
                .title("테스트 모임 생성")
                .date(nowDate.plusDays(7))
                .dateVoteDeadline(nowDateTime.plusDays(1))
                .courseVoteDeadline(nowDateTime.plusDays(3))
                .build();
        when(request.toMeetingEntity()).thenReturn(meeting);

        Member member = new Member(
                "1234@example.com",
                "닉네임",
                LocalDate.of(2000,1,1),
                Gender.FEMALE,
                MemberRole.MEMBER,
                CharacterType.TRAVELER
                );
        // Todo: 추후 변경된 로직에 맞게 변경 필요.
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        meetingService.createMeeting(memberId, request);

        // then
        verify(meetingRepository, times(1)).save(meeting);
        ArgumentCaptor<MeetingParticipant> captor = ArgumentCaptor.forClass(MeetingParticipant.class);
        verify(meetingParticipantRepository, times(1)).save(captor.capture());

        MeetingParticipant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getMeeting()).isSameAs(meeting);
        assertThat(savedParticipant.getMember()).isSameAs(member);
        assertThat(savedParticipant.getRole()).isEqualTo(pyws.swyp.meeting.entity.ParticipantRole.HOST);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 모임 생성 시 실패")
    void 모임_생성_실패_존재하지_않는_유저() {
        // given
        Long memberId = 10L;
        MeetingCreateRequest request = mock(MeetingCreateRequest.class);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meetingService.createMeeting(memberId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                });

        verify(meetingRepository, never()).save(any());
        verify(meetingParticipantRepository, never()).save(any());
    }

    /*
    * 모임 삭제
    */

    @Test
    @DisplayName("모임이 존재하고, 삭제하려는 사람이 모임장이라면 모임 삭제 성공")
    void 모임_삭제_성공() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant meetingParticipant = mock(MeetingParticipant.class);
        when(meetingParticipant.getRole()).thenReturn(ParticipantRole.HOST);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(meetingParticipant));

        // when
        meetingService.deleteMeeting(memberId, meetingId);

        // then
        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);
        verify(meeting).delete();
    }

    @Test
    @DisplayName("없는 모임이라면 삭제 실패")
    void 모임_삭제_실패_없는_모임() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meetingService.deleteMeeting(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_NOT_FOUND);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository, never()).findByMemberIdAndMeetingId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("비활성 모임이라면 삭제 실패")
    void 모임_삭제_실패_비활성_모임() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(false);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when & then
        assertThatThrownBy(() -> meetingService.deleteMeeting(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                   CustomException ce = (CustomException) ex;
                   assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_NOT_FOUND);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository, never()).findByMemberIdAndMeetingId(anyLong(), anyLong());
        verify(meeting, never()).delete();
    }

    @Test
    @DisplayName("HOST가 아닌 유저라면 모임 삭제 실패")
    void 모임_삭제_실패_HOST_아닌_유저() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant meetingParticipant = mock(MeetingParticipant.class);
        when(meetingParticipant.getRole()).thenReturn(pyws.swyp.meeting.entity.ParticipantRole.MEMBER);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(meetingParticipant));

        // when & then
        assertThatThrownBy(() -> meetingService.deleteMeeting(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                   CustomException ce = (CustomException) ex;
                   assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_ACCESS_DENIED);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);
        verify(meeting, never()).delete();
    }

    /*
    * 모임 탈퇴
    */

    @Test
    @DisplayName("모임이 존재하고, 탈퇴하려는 사람이 모임 구성원이라면 모임 삭제 성공")
    void 모임_탈퇴_성공() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant meetingParticipant = mock(MeetingParticipant.class);
        when(meetingParticipant.getRole()).thenReturn(pyws.swyp.meeting.entity.ParticipantRole.MEMBER);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(meetingParticipant));

        // when
        meetingService.quitMeeting(memberId, meetingId);

        // then
        verify(meetingRepository, times(1)).findById(meetingId);
        verify(meetingParticipantRepository, times(1)).findByMemberIdAndMeetingId(memberId, meetingId);
        verify(meetingParticipantRepository, times(1)).delete(meetingParticipant);
    }

    @Test
    @DisplayName("모임 구성원이 아니라면 탈퇴 실패")
    void 모임_탈퇴_실패_구성원_아닌_유저() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meetingService.quitMeeting(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                   CustomException ce = (CustomException) ex;
                   assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_ACCESS_DENIED);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);
        verify(meetingParticipantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("HOST라면 모임 탈퇴 실패")
    void 모임_탈퇴_실패_HOST인_유저() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant meetingParticipant = mock(MeetingParticipant.class);
        when(meetingParticipant.getRole()).thenReturn(pyws.swyp.meeting.entity.ParticipantRole.HOST);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(meetingParticipant));

        // when & then
        assertThatThrownBy(() -> meetingService.quitMeeting(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                   CustomException ce = (CustomException) ex;
                   assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_QUIT_DENIED);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);
        verify(meetingParticipantRepository, never()).delete(any());
    }

    /*
    * 모임 수정
    */

    @Test
    @DisplayName("하나의 필드만 들어오더라도 모임 수정 성공")
    void 모임_수정_성공() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = Meeting.builder()
                .title("모잇 오프라인 모임")
                .date(null)
                .dateVoteDeadline(LocalDateTime.of(2025,12,31,13,00))
                .courseVoteDeadline(null)
                .build();

        MeetingUpdateRequest request = new MeetingUpdateRequest(
                null,
                LocalDate.of(2026,1,20),
                null,
                null
        );

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant participant = mock(MeetingParticipant.class);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(participant));

        // when
        meetingService.updateMeeting(memberId, meetingId, request);

        // then
        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);

        assertThat(meeting.getTitle()).isEqualTo("모잇 오프라인 모임");
        assertThat(meeting.getDate()).isEqualTo(LocalDate.of(2026,1,20));
        assertThat(meeting.getDateVoteDeadline()).isEqualTo(LocalDateTime.of(2025,12,31,13,00));
        assertThat(meeting.getCourseVoteDeadline()).isNull();
    }

    @Test
    @DisplayName("구성원이 아닌 유저가 수정 요청 시 실패")
    void 모임_수정_실패_구성원_아닌_유저() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = Meeting.builder()
                .title("모잇 오프라인 모임")
                .date(null)
                .dateVoteDeadline(LocalDateTime.of(2025,12,31,13,00))
                .courseVoteDeadline(null)
                .build();

        MeetingUpdateRequest request = new MeetingUpdateRequest(
                null,
                LocalDate.of(2026,1,20),
                null,
                null
        );

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meetingService.updateMeeting(memberId, meetingId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                   CustomException ce = (CustomException) ex;
                   assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_ACCESS_DENIED);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);

        assertThat(meeting.getTitle()).isEqualTo("모잇 오프라인 모임");
        assertThat(meeting.getDate()).isNull();
        assertThat(meeting.getDateVoteDeadline()).isEqualTo(LocalDateTime.of(2025,12,31,13,00));
        assertThat(meeting.getCourseVoteDeadline()).isNull();
    }

    @Test
    @DisplayName("수정할 제목이 null이 아니고, Blank로 들어오면 수정 실패")
    void 모임_수정_실패_제목_Blank() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = Meeting.builder()
                .title("모잇 오프라인 모임")
                .date(null)
                .dateVoteDeadline(LocalDateTime.of(2025,12,31,13,00))
                .courseVoteDeadline(null)
                .build();

        MeetingUpdateRequest request = new MeetingUpdateRequest(
                " ",
                LocalDate.of(2026,1,20),
                null,
                null
        );

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant participant = mock(MeetingParticipant.class);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> meetingService.updateMeeting(memberId, meetingId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                   CustomException ce = (CustomException) ex;
                   assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEETING_TITLE_EMPTY);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMemberIdAndMeetingId(memberId, meetingId);

        assertThat(meeting.getTitle()).isEqualTo("모잇 오프라인 모임");
        assertThat(meeting.getDate()).isNull();
        assertThat(meeting.getDateVoteDeadline()).isEqualTo(LocalDateTime.of(2025,12,31,13,00));
        assertThat(meeting.getCourseVoteDeadline()).isNull();

    }

    /*
    * 모임 조회
    */

    @Test
    @DisplayName("모임 전체 조회 성공")
    void 모임_전체_조회_성공() {
        // given
        Long memberId = 1L;

        List<MeetingBriefResponse> response = List.of(mock(MeetingBriefResponse.class));
        when(meetingParticipantRepository.findMeetingsByMemberId(memberId)).thenReturn(response);

        // when
        List<MeetingBriefResponse> result = meetingService.getAllMeetings(memberId);

        // then
        assertThat(result).isEqualTo(response);
        verify(meetingParticipantRepository, times(1)).findMeetingsByMemberId(memberId);
    }

    @Test
    @DisplayName("대기 중 모임 조회 성공 - CREATED, DATE_VOTING, PLACE_VOTING 상태만 조회")
    void 대기중_모임_조회_성공() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0,10);

        List<MeetingBriefResponse> response = List.of(mock(MeetingBriefResponse.class));

        when(meetingParticipantRepository.findMeetingsByMemberIdAndStatus(
                eq(memberId),
                anyList(),
                eq(pageable)
        )).thenReturn(response);

        // when
        List<MeetingBriefResponse> result = meetingService.getWaitingMeetings(memberId, pageable);

        // then
        assertThat(result).isEqualTo(response);
        verify(meetingParticipantRepository, times(1))
                .findMeetingsByMemberIdAndStatus(
                        eq(memberId),
                        eq(List.of(
                                MeetingStatus.CREATED,
                                MeetingStatus.DATE_VOTING,
                                MeetingStatus.PLACE_VOTING
                        )),
                        eq(pageable)
                );
    }

}
