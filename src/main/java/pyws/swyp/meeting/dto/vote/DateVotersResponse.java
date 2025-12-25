package pyws.swyp.meeting.dto.vote;

import java.util.List;

public record DateVotersResponse(
        List<DateVoterResponse> voters
) {
}
