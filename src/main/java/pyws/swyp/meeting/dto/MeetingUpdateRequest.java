package pyws.swyp.meeting.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MeetingUpdateRequest(
        String title,
        LocalDate date
) {
}
