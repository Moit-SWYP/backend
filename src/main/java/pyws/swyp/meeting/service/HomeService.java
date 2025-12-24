package pyws.swyp.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.meeting.dto.*;
import pyws.swyp.meeting.entity.Status;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MeetingParticipantRepository meetingParticipantRepository;

    private static final int DEFAULT_TOP = 3;

    @Transactional(readOnly = true)
    public HomeResponse getHomeInfo(Long memberId) {
        // 홈 상단 예정된 일정 카드
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate endDate = now.plusDays(7);
        List<MeetingBriefResponse> meetingInfos = meetingParticipantRepository.findMeetingsByMemberIdWithin7Days(memberId, now, endDate);

        List<Long> meetingIds = meetingInfos.stream()
                .map(MeetingBriefResponse::meetingId)
                .toList();

        Map<Long, List<ParticipantResponse>> participants
                = meetingIds.isEmpty() ? Map.of()
                : meetingParticipantRepository.findByMeetingIds(meetingIds).stream()
                .collect(Collectors.groupingBy(
                        ParticipantRow::meetingId,
                        Collectors.mapping(ParticipantRow::participant, Collectors.toList())
                ));

        List<MeetingBriefWithParticipantsResponse> upcomingMeetings =
                meetingInfos.stream()
                        .map(info -> new MeetingBriefWithParticipantsResponse(
                                info.meetingId(),
                                info.title(),
                                info.status(),
                                info.date(),
                                participants.getOrDefault(info.meetingId(), List.of())
                        ))
                        .toList();

        // 홈 하단 기다리고 있는 일정 카드
        List<Status> statuses = List.of(Status.CREATED, Status.DATE_VOTING, Status.PLACE_VOTING);
        List<MeetingBriefResponse> waitingMeetings
                = meetingParticipantRepository.findMeetingsByMemberIdAndStatus(
                        memberId, statuses, PageRequest.of(0, DEFAULT_TOP));

        return new HomeResponse(upcomingMeetings, waitingMeetings);
    }
}
