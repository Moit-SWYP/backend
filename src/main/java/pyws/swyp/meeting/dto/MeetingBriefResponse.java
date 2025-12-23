package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.Status;

import java.time.LocalDateTime;

public record MeetingBriefResponse(
        Long meetingId,
        String title,
        Status status,
        LocalDateTime date
) {
}
