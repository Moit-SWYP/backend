package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.Status;

import java.time.LocalDate;

public record MeetingBriefResponse(
        Long meetingId,
        String title,
        Status status,
        LocalDate date
) {
}
