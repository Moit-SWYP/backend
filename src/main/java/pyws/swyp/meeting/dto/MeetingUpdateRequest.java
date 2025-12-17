package pyws.swyp.meeting.dto;

import java.time.LocalDateTime;

public record MeetingUpdateRequest(
        String title,
        LocalDateTime date,
        LocalDateTime dateVoteDeadline,
        LocalDateTime courseVoteDeadline
) {
}
