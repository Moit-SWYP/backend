package pyws.swyp.meeting.dto.vote.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record VotedTimeResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,
        long count
) {
}
