package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.MeetingType;

public record MeetingUpdateRequest(
        String title,
        MeetingType type
) {
}
