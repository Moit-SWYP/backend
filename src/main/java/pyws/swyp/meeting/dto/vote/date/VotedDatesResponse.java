package pyws.swyp.meeting.dto.vote.date;

import java.time.LocalDate;
import java.util.List;

public record VotedDatesResponse(
        List<LocalDate> dates
) {
}
