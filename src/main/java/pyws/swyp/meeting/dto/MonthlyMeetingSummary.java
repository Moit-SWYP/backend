package pyws.swyp.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record MonthlyMeetingSummary(
        Long meetingId,
        String title,
        LocalDate date,
        String dayOfWeek,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,


        boolean isDateVoteDone,
        boolean isTimeVoteDone,
        boolean isCourseVoteDone,

        int courseCount,

        boolean hasReview,
        String reviewImageUrl,
        String reviewContent
) {
}
