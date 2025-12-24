package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.Status;

import java.time.LocalDate;
import java.util.List;

public record MeetingBriefWithParticipantsResponse(
        Long meetingId,
        String title,
        Status status,
        LocalDate date,
        List<ParticipantResponse> participants
) {
}
