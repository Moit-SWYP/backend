package pyws.swyp.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import pyws.swyp.meeting.entity.MeetingType;

public record MonthlyMeetingSummary(
        Long meetingId,
        String title,
        LocalDate date,
        DayOfWeek dayOfWeek,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,
        MeetingType meetingType,
        boolean courseVoteDone,
        int courseCount,
        List<String> recordImageUrls,
        String recordContent
) {
}
