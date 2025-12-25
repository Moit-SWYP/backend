package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.MeetingStatus;

import java.time.LocalDate;

public record MeetingBriefResponse(
        Long meetingId,
        String title,
        MeetingStatus status,
        LocalDate date
) {
}
