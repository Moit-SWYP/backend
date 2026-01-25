package pyws.swyp.meeting.dto.vote;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import pyws.swyp.meeting.dto.vote.date.DateSummary;
import pyws.swyp.meeting.dto.vote.time.TimeSummary;
import pyws.swyp.meeting.entity.MeetingStatus;

public record VoteSummary(
        boolean isHost,
        LocalDate confirmedDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime confirmedTime,
        DateSummary dateSummary,
        TimeSummary timeSummary
) {
}
