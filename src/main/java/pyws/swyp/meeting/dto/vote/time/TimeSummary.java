package pyws.swyp.meeting.dto.vote.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;

public record TimeSummary(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        List<LocalTime> topTimes,
        List<VotedTimeResponse> votedTimes
) {
}
