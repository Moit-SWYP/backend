package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.MeetingStatus;

import java.time.LocalDate;
import java.util.List;

public record MeetingBriefWithParticipantsResponse(
        Long meetingId,
        String title,
        MeetingStatus status,
        LocalDate date,
        List<ParticipantInfo> participants
) {
}
