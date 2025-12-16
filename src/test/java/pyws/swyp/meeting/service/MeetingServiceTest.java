package pyws.swyp.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.Role;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MeetingService meetingService;

    LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("모임 생성에 성공한다")
    void 모임_생성_성공() {
        // given
        MeetingCreateRequest request = mock(MeetingCreateRequest.class);
        Meeting meeting = Meeting.builder()
                .title("테스트 모임 생성")
                .date(now.plusDays(7))
                .dateVoteDeadline(now.plusDays(1))
                .courseVoteDeadline(now.plusDays(3))
                .build();
        when(request.toMeetingEntity()).thenReturn(meeting);

        Member member = new Member(
                "1234@example.com",
                "닉네임",
                LocalDate.of(2000,1,1),
                Gender.FEMALE,
                pyws.swyp.member.entity.Role.MEMBER
                );
        // Todo: 추후 변경된 로직에 맞게 변경 필요.
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        meetingService.createMeeting(request);

        // then
        verify(meetingRepository, times(1)).save(meeting);
        ArgumentCaptor<MeetingParticipant> captor = ArgumentCaptor.forClass(MeetingParticipant.class);
        verify(meetingParticipantRepository, times(1)).save(captor.capture());

        MeetingParticipant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getMeeting()).isSameAs(meeting);
        assertThat(savedParticipant.getMember()).isSameAs(member);
        assertThat(savedParticipant.getRole()).isEqualTo(Role.HOST);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 모임 생성 시 실패")
    void 모임_생성_실패_존재하지_않는_유저() {
        // given
        MeetingCreateRequest request = mock(MeetingCreateRequest.class);
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meetingService.createMeeting(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                });

        verify(meetingRepository, never()).save(any());
        verify(meetingParticipantRepository, never()).save(any());
    }
}
