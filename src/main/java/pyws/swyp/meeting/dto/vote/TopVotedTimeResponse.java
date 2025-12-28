package pyws.swyp.meeting.dto.vote;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.List;

public record TopVotedTimeResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        List<LocalTime> times
) {
}
