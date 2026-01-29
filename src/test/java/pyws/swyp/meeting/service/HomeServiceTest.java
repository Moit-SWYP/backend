package pyws.swyp.meeting.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pyws.swyp.meeting.dto.*;
import pyws.swyp.meeting.entity.MeetingType;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.member.entity.CharacterType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeServiceTest {
    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;

    @InjectMocks
    private HomeService homeService;

    @Test
    void 홈화면_조회_성공() {
        // given
        Long memberId = 1L;
        LocalDate fixedNow = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate fixedEnd = fixedNow.plusDays(7);

        List<MeetingBriefResponse> meetingInfos = List.of(
                new MeetingBriefResponse(1L, "모잇 오프라인 모임", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, fixedNow.plusDays(4), null, false),
                new MeetingBriefResponse(2L, "카공", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, fixedNow.plusDays(6), LocalTime.of(14,30),true),
                new MeetingBriefResponse(3L, "데이트", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, fixedNow.plusDays(2), LocalTime.of(18,0), true)
        );

        ParticipantInfo p1 = new ParticipantInfo(memberId, "닉네임1", CharacterType.FOODIE, pyws.swyp.meeting.entity.ParticipantRole.HOST);
        ParticipantInfo p2 = new ParticipantInfo(2L, "닉네임2", CharacterType.TRAVELER, pyws.swyp.meeting.entity.ParticipantRole.MEMBER);
        ParticipantInfo p3 = new ParticipantInfo(3L, "닉네임3", CharacterType.HEALER, ParticipantRole.MEMBER);
        ParticipantInfo p4 = new ParticipantInfo(4L, "닉네임4", CharacterType.STUDIER, pyws.swyp.meeting.entity.ParticipantRole.MEMBER);
        ParticipantInfo p5 = new ParticipantInfo(5L, "닉네임5", CharacterType.FOODIE, pyws.swyp.meeting.entity.ParticipantRole.MEMBER);

        List<ParticipantRow> participantRows = List.of(
                new ParticipantRow(1L, p1),
                new ParticipantRow(2L, p1),
                new ParticipantRow(3L, p1),
                new ParticipantRow(1L, p2),
                new ParticipantRow(1L, p3),
                new ParticipantRow(2L, p4),
                new ParticipantRow(3L, p5)
        );

        List<MeetingBriefResponse> waitingMeetings = List.of(
                new MeetingBriefResponse(1L, "모잇 오프라인 모임", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, fixedNow.plusDays(4), null, false),
                new MeetingBriefResponse(2L, "카공", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, fixedNow.plusDays(6), LocalTime.of(14,30),false),
                new MeetingBriefResponse(4L, "보드게임 모임", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, null, null, true),
                new MeetingBriefResponse(5L, "동아리 전체 회식", MeetingType.TRAVELER, MeetingStatus.IN_PROGRESS, null, null, false)
        );

        when(meetingParticipantRepository.findMeetingsByMemberIdWithin7Days(memberId, fixedNow, fixedEnd))
                .thenReturn(meetingInfos);

        when(meetingParticipantRepository.findByMeetingIds(anyList()))
                .thenReturn(participantRows);

        when(meetingParticipantRepository.findWaitingMeetingsByMemberId(
                eq(memberId),
                anyList(),
                any()
        )).thenReturn(waitingMeetings);

        // when
        try(MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixedNow);

            HomeResponse result = homeService.getHomeInfo(memberId);

            // then
            assertThat(result.homeMeetings()).hasSize(3);

            MeetingBriefWithParticipantsResponse first = result.homeMeetings().get(0);
            assertThat(first.meetingId()).isEqualTo(1L);
            assertThat(first.participants()).hasSize(3);

            MeetingBriefWithParticipantsResponse second = result.homeMeetings().get(1);
            assertThat(second.meetingId()).isEqualTo(2L);
            assertThat(second.participants()).hasSize(2);

            MeetingBriefWithParticipantsResponse third = result.homeMeetings().get(2);
            assertThat(third.meetingId()).isEqualTo(3L);
            assertThat(third.participants()).hasSize(2);

            assertThat(result.waitingMeetings()).isEqualTo(waitingMeetings);
        }
    }
}
