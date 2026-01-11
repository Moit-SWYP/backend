package pyws.swyp.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.InvitationLinkResponse;
import pyws.swyp.meeting.dto.InviteFriendsRequest;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.friend.Friendship;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.friend.FriendshipRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceTest {
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    @DisplayName("모임 초대 토큰 조회에 성공한다")
    void 모임_초대_토큰_조회_성공() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;
        UUID publicId = UUID.randomUUID();

        Meeting meeting = mock(Meeting.class);
        when(meeting.getPublicId()).thenReturn(publicId);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        MeetingParticipant participant = mock(MeetingParticipant.class);
        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(participant));

        // when
        InvitationLinkResponse response =
                invitationService.createInvitationLink(memberId, meetingId);

        // then
        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository)
                .findByMemberIdAndMeetingId(memberId, meetingId);

        assertThat(response.meetingId()).isEqualTo(meetingId);
        assertThat(response.inviteToken()).isEqualTo(publicId.toString());
    }

    @Test
    @DisplayName("모임 초대 토큰 조회에 성공한다")
    void 모임_초대_토큰_조회_실패_비활성화_모임() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(false);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when & then
        assertThatThrownBy(() ->
                invitationService.createInvitationLink(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode())
                            .isEqualTo(ErrorCode.MEETING_NOT_FOUND);
                });

        verify(meetingRepository).findById(meetingId);
    }

    @Test
    @DisplayName("구성원이 아닌 유저가 모임 초대 토큰 조회 시 실패한다")
    void 모임_초대_토큰_조회_실패_구성원_아님() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        Meeting meeting = mock(Meeting.class);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                invitationService.createInvitationLink(memberId, meetingId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode())
                            .isEqualTo(ErrorCode.MEETING_ACCESS_DENIED);
                });

        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository)
                .findByMemberIdAndMeetingId(memberId, meetingId);
    }

    @Test
    @DisplayName("초대 링크로 모임 참여에 성공한다")
    void 초대_링크로_모임_참여_성공() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;
        String inviteToken = UUID.randomUUID().toString();

        Meeting meeting = mock(Meeting.class);
        when(meeting.getId()).thenReturn(meetingId);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findByPublicId(UUID.fromString(inviteToken)))
                .thenReturn(Optional.of(meeting));

        Member member = mock(Member.class);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(meetingParticipantRepository
                .existsByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(false);

        // when
        invitationService.joinMeetingFromLink(memberId, inviteToken);

        // then
        verify(meetingRepository).findByPublicId(UUID.fromString(inviteToken));
        verify(memberRepository).findById(memberId);
        verify(meetingParticipantRepository)
                .existsByMemberIdAndMeetingId(memberId, meetingId);
        verify(meetingParticipantRepository).save(any(MeetingParticipant.class));
    }

    @Test
    @DisplayName("이미 모임에 참여한 유저는 초대 링크로 모임 참여에 실패한다.")
    void 초대_링크로_모임_참여_실패_이미_참여() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;
        String inviteToken = UUID.randomUUID().toString();

        Meeting meeting = mock(Meeting.class);
        when(meeting.getId()).thenReturn(meetingId);
        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findByPublicId(UUID.fromString(inviteToken)))
                .thenReturn(Optional.of(meeting));

        Member member = mock(Member.class);
        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));

        when(meetingParticipantRepository
                .existsByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                invitationService.joinMeetingFromLink(memberId, inviteToken))
                .isInstanceOf(CustomException.class)
                        .satisfies(ex -> {
                            CustomException ce = (CustomException) ex;
                            assertThat(ce.getErrorCode())
                                    .isEqualTo(ErrorCode.MEETING_ALREADY_JOINED);
                        });

        // then
        verify(meetingRepository).findByPublicId(UUID.fromString(inviteToken));
        verify(memberRepository).findById(memberId);
        verify(meetingParticipantRepository)
                .existsByMemberIdAndMeetingId(memberId, meetingId);
        verify(meetingParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("친구 목록에서 memberId들을 통해 모임에 초대 성공")
    void 친구_목록에서_초대_성공() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;
        InviteFriendsRequest request = new InviteFriendsRequest(List.of(2L, 3L));

        Meeting meeting = mock(Meeting.class);
        Member memberRef = mock(Member.class);
        Member friendRef2 = mock(Member.class);
        Member friendRef3 = mock(Member.class);

        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        when(memberRepository.findAllById(List.of(2L, 3L)))
                .thenReturn(List.of(friendRef2, friendRef3));

        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(mock(MeetingParticipant.class)));

        when(meetingParticipantRepository.findOtherMemberIdsByMeetingId(memberId, meetingId))
                .thenReturn(List.of());

        when(entityManager.getReference(Member.class, 2L)).thenReturn(friendRef2);
        when(entityManager.getReference(Member.class, 3L)).thenReturn(friendRef3);

        when(meetingParticipantRepository.findMemberIdsByMeetingId(meetingId))
                .thenReturn(List.of(memberId, 2L, 3L));
        when(friendshipRepository.findByMemberIds(any()))
                .thenReturn(List.of());

        // when
        invitationService.inviteToMeeting(memberId, meetingId, request);

        // then
        // 모임원 추가 검증
        ArgumentCaptor<List<MeetingParticipant>> participantCaptor = ArgumentCaptor.forClass(List.class);
        verify(meetingParticipantRepository).saveAll(participantCaptor.capture());
        assertThat(participantCaptor.getValue()).hasSize(2);

        // 친구 관계 저장 검증
        ArgumentCaptor<List<Friendship>> friendshipCaptor = ArgumentCaptor.forClass(List.class);
        verify(friendshipRepository).saveAll(friendshipCaptor.capture());
        assertThat(friendshipCaptor.getValue()).hasSize(6);
    }

    @Test
    @DisplayName("이미 모임에 참여 중인 멤버들이라면 아무것도 저장하지 않는다.")
    void 친구_목록에서_초대시_이미_참여중_멤버라면_아무것도_저장하지_않음() {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;
        InviteFriendsRequest request = new InviteFriendsRequest(List.of(2L, 3L));

        Meeting meeting = mock(Meeting.class);
        Member friendRef2 = mock(Member.class);
        Member friendRef3 = mock(Member.class);

        when(meeting.isActive()).thenReturn(true);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        when(memberRepository.findAllById(List.of(2L, 3L)))
                .thenReturn(List.of(friendRef2, friendRef3));

        when(meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId))
                .thenReturn(Optional.of(mock(MeetingParticipant.class)));

        when(meetingParticipantRepository.findOtherMemberIdsByMeetingId(memberId, meetingId))
                .thenReturn(List.of(2L, 3L));

        // when
        invitationService.inviteToMeeting(memberId, meetingId, request);

        // then
        // 모임원 추가 검증
        verify(meetingParticipantRepository, never()).saveAll(any());
        verify(friendshipRepository, never()).saveAll(any());
    }

}
