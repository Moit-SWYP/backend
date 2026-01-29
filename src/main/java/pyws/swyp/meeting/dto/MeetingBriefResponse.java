package pyws.swyp.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.MeetingType;

import java.time.LocalDate;
import java.time.LocalTime;

public record MeetingBriefResponse(
        Long meetingId,
        String title,
        MeetingType type,
        MeetingStatus status,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        Boolean courseFixed
) {
}
