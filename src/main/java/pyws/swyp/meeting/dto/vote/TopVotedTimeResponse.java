package pyws.swyp.meeting.dto.vote;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record TopVotedTimeResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time) {
}
