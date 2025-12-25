package pyws.swyp.meeting.dto.vote;

import java.time.LocalDate;
import java.util.List;

public record VotedDatesResponse(
        List<LocalDate> dates
) {
}
