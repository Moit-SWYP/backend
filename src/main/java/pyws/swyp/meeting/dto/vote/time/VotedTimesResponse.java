package pyws.swyp.meeting.dto.vote.time;

import java.util.List;

public record VotedTimesResponse(
        List<VotedTimeResponse> times
) {
}
