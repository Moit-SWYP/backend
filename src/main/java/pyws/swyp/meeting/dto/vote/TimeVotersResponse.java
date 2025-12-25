package pyws.swyp.meeting.dto.vote;

import java.util.List;

public record TimeVotersResponse(
        List<TimeVoterResponse> voters
) {
}
